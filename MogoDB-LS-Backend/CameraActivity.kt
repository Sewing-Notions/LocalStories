package com.sensorreadings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            uploadImageToServer(imageUri)
        }
    }

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permissions are required to use the camera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val cameraButton = findViewById<Button>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            checkPermissionsAndOpenCamera()
        }
    }

    private fun checkPermissionsAndOpenCamera() {
        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun openCamera() {
        val imageFile = File.createTempFile("IMG_", ".jpg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            imageFile
        )
        takePictureLauncher.launch(imageUri)
    }

    private fun uploadImageToServer(imageUri: Uri) {
        val file = File(imageUri.path!!)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        // Example form fields
        val storyId = "123".toRequestBody("text/plain".toMediaTypeOrNull())
        val title = "My Title".toRequestBody("text/plain".toMediaTypeOrNull())
        val description = "A description".toRequestBody("text/plain".toMediaTypeOrNull())
        val dateOfFact = "2025-06-14".toRequestBody("text/plain".toMediaTypeOrNull())
        val locationId = "456".toRequestBody("text/plain".toMediaTypeOrNull())
        val userId = "789".toRequestBody("text/plain".toMediaTypeOrNull())

        val retrofit = Retrofit.Builder()
            .baseUrl("http://35.247.54.23:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.uploadStory(body, storyId, title, description, dateOfFact, locationId, userId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("Upload", "Success")
                } else {
                    Log.e("Upload", "Failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload", "Error: ${t.message}")
            }
        })
    }
}

interface ApiService {
    @Multipart
    @POST("add_story")
    fun uploadStory(
        @Part photo: MultipartBody.Part,
        @Part("storyId") storyId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("dateOfFact") dateOfFact: RequestBody,
        @Part("locationId") locationId: RequestBody,
        @Part("userId") userId: RequestBody
    ): Call<ResponseBody>
}
