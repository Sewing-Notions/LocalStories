
package com.localstories

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import java.io.InputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            //uploadImageToServer(imageUri)
            // return imageUri to AddStoryActivity
            val resultIntent = Intent()
            resultIntent.putExtra("imageUri", imageUri)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        //android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permissions are required to use the camera", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionsAndOpenCamera()
    }

    fun checkPermissionsAndOpenCamera() {
        if (permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun openCamera() {
        val imageFile: File
        try {
            imageFile = File.createTempFile("IMG_", ".jpg", cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
        } catch (ex: Exception) {
            Log.e("CameraActivity", "Error creating temp file for camera", ex)
            Toast.makeText(this, "Error preparing camera.", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        imageUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile)
        takePictureLauncher.launch(imageUri)
    }


    private fun uploadImageToServer(imageUri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("Upload", "Failed to get InputStream from URI")
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }

            val requestFile = inputStream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
            val fileName = imageUri.lastPathSegment ?: "image.jpg"
            val body = MultipartBody.Part.createFormData("photo", fileName, requestFile)

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
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Upload", "Success")
                        setResult(Activity.RESULT_OK)
                    } else {
                        Log.e("Upload", "Failed: ${response.code()}")
                    }
                    finish()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "Error: ${t.message}")
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            })
        } catch (e: Exception) {
            Log.e("Upload", "Error processing image for upload", e)
            Toast.makeText(this, "Error processing image.", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        /*val file = File(imageUri.path!!)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

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
        }) */
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
