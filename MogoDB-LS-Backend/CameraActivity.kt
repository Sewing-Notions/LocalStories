
package com.sensorreadings

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            uploadImageToServer(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Example button to trigger camera
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            checkPermissionsAndOpenCamera()
        }
    }

private val permissions = arrayOf(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.READ_EXTERNAL_STORAGE
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

private fun checkPermissionsAndOpenCamera() {
    if (permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }) {
        checkPermissionsAndOpenCamera()
    } else {
        requestPermissionLauncher.launch(permissions)
    }
}


    private fun uploadImageToServer(imageUri: Uri) {
        val file = File(imageUri.path!!)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://35.247.54.23:3000/") // PORT
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.uploadStory(body, /* other fields */)

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
