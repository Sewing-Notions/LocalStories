
---------------------------
network_security_config.xml
---------------------------
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">35.247.54.23</domain>
  </domain-config>
</network-security-config>

---------------------------
file_paths.xml
---------------------------
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
  <cache-path name="images" path="." />
</paths>


MainActivity.kt
import androidx.activity.compose.setContent

build.gradle

buildFeatures {
    compose true }
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.10" }
dependencies {
    implementation "androidx.activity:activity-compose:1.8.0" 
    implementation "androidx.compose.ui:ui:1.5.0"
    implementation "androidx.compose.material3:material3:1.1.0"
    implementation "androidx.compose.ui:ui-tooling-preview:1.5.0"
    debugImplementation "androidx.compose.ui:ui-tooling:1.5.0" }
----------------------------
MainActivity.kt
----------------------------
package com.example.imagetransferandroid

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.imagetransferandroid.ui.theme.ImageTransferAndroidTheme
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageTransferAndroidTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AddStoryScreen(modifier = Modifier.padding(innerPadding))
    }
}
fun uploadStory(context: Context, imageUri: Uri) {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri) ?: return
    val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
    tempFile.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
    }

    val client = OkHttpClient()
    val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("photo", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaType()))
        .addFormDataPart("storyId", "story999")
        .addFormDataPart("title", "Test Story")
        .addFormDataPart("description", "This is a test upload")
        .addFormDataPart("dateOfFact", "2025-06-13")
        .addFormDataPart("locationId", "loc001")
        .addFormDataPart("userId", "user001")
        .build()

    val request = Request.Builder()
        .url("http://35.247.54.23:3000/add_story")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("Upload", "Failed: ${e.message}")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseText = response.body?.string()
            Log.d("Upload", "Success: $responseText")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
            }
        }
    })
}

@Composable
fun AddStoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val photoFile = remember {
        File(context.cacheDir, "captured_photo.jpg").apply { createNewFile() }
    }

    val photoUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        photoFile
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            Toast.makeText(context, "Permissions are required to use this feature", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri.value = photoUri
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri.value = uri
    }

    Column(modifier = modifier.padding(16.dp)) {
        Button(onClick = { pickImageLauncher.launch("image/*") }) {
            Text("Pick Image")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { takePictureLauncher.launch(photoUri) }) {
            Text("Capture Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            imageUri.value?.let {
                uploadStory(context, it)
            }
        }) {
            Text("Upload Story")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImageTransferAndroidTheme {
        Text("Hello Android")
    }
}
----------------------------
build.gradle
----------------------------
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace 'com.example.imagetransferandroid'
    compileSdk 35

    defaultConfig {
        applicationId "com.example.imagetransferandroid"
        minSdk 21
        targetSdk 35
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }


    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10" // or latest stable
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = '11'
    }
    buildFeatures {
        compose true
    }
}

dependencies {

    implementation libs.androidx.activity.compose.v180 // or latest
    implementation libs.ui
    implementation libs.material3
    implementation libs.ui.tooling.preview
    debugImplementation libs.ui.tooling

    implementation libs.androidx.core.ktx
    implementation libs.androidx.lifecycle.runtime.ktx
    implementation libs.androidx.activity.compose
    implementation platform(libs.androidx.compose.bom)
    implementation libs.androidx.ui
    implementation libs.androidx.ui.graphics
    implementation libs.androidx.ui.tooling.preview
    implementation libs.androidx.material3
    testImplementation libs.junit
    androidTestImplementation libs.androidx.junit
    androidTestImplementation libs.androidx.espresso.core
    androidTestImplementation platform(libs.androidx.compose.bom)
    androidTestImplementation libs.androidx.ui.test.junit4
    debugImplementation libs.androidx.ui.tooling
    debugImplementation libs.androidx.ui.test.manifest
    implementation(libs.okhttp)
}
----------------------------
AndroidManifest.xml
-----------------------------
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.INTERNET" />


    <application
        android:networkSecurityConfig="@xml/network_security_config"
        android:usesCleartextTraffic="true"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ImageTransferAndroid"
        tools:targetApi="31">

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.ImageTransferAndroid">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>

