package com.laila.sustainwise.ui.editprofile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.R
import com.laila.sustainwise.data.retrofit.ApiService
import com.laila.sustainwise.data.retrofit.RetrofitInstance
import com.laila.sustainwise.ui.main.MainActivity
import com.laila.sustainwise.ui.signup.DeletePhotoResponse
import com.laila.sustainwise.ui.signup.EditUserRequest
import com.laila.sustainwise.ui.signup.EditUserResponse
import com.laila.sustainwise.ui.signup.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var ivProfilePicture: ImageView
    private var selectedPhotoUri: Uri? = null
    private lateinit var btnDelete: Button
    private lateinit var btnUpdate: Button
    private lateinit var ed_signup_name: EditText
    private lateinit var btnSave: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        // Initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnDelete = findViewById(R.id.btnDelete)
        btnUpdate = findViewById(R.id.btnUpdate)
        ed_signup_name = findViewById(R.id.ed_signup_name)
        btnSave = findViewById(R.id.btnSave)

        auth = FirebaseAuth.getInstance()
        apiService = RetrofitInstance.api

        // Fetch and display current user profile
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = "Bearer ${result.token}"
            getUserProfile(idToken)
        }

        // Open image picker to update profile picture
        btnUpdate.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // Delete selected photo
        btnDelete.setOnClickListener {
            val currentPhotoUri = selectedPhotoUri
            if (currentPhotoUri != null) {
                // Foto berasal dari galeri
                AlertDialog.Builder(this)
                    .setTitle("Are you sure?")
                    .setMessage("Do you want to remove the selected photo?")
                    .setPositiveButton("Yes") { _, _ ->
                        selectedPhotoUri = null // Hapus URI foto yang dipilih
                        ivProfilePicture.setImageResource(R.drawable.baseline_camera_alt_24) // Gambar default
                        // Cek apakah ada foto dari Firebase untuk menampilkan kembali
                        val currentUser = auth.currentUser
                        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                            val idToken = "Bearer ${result.token}"
                            getUserProfile(idToken) // Ambil kembali foto dari Firebase jika ada
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                // Foto berasal dari Firebase
                AlertDialog.Builder(this)
                    .setTitle("Are you sure?")
                    .setMessage("Do you really want to delete your profile photo from Firebase?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteProfilePhoto() // Jalankan logika API untuk menghapus foto
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }



        // Save the updated username and photo
        btnSave.setOnClickListener {
            val newUsername = ed_signup_name.text.toString()
            if (newUsername.isNotEmpty()) {
                updateUserProfile(newUsername)
            }

            selectedPhotoUri?.let {
                val base64String = convertImageToBase64(it)
                if (base64String != null) {
                    updateProfilePhotoBase64(base64String)
                } else {
                    Toast.makeText(this, "Failed to convert image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteProfilePhoto() {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = "Bearer ${result.token}"

            // Call the API to delete the photo
            apiService.deleteProfilePhoto(idToken).enqueue(object : Callback<DeletePhotoResponse> {
                override fun onResponse(call: Call<DeletePhotoResponse>, response: Response<DeletePhotoResponse>) {
                    if (response.isSuccessful) {
                        // Successfully deleted photo
                        ivProfilePicture.setImageResource(R.drawable.baseline_camera_alt_24) // Default image
                        Toast.makeText(this@EditProfileActivity, "Profile photo deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("DeleteProfilePhoto", "Failed to delete photo. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<DeletePhotoResponse>, t: Throwable) {
                    Log.e("DeleteProfilePhoto", "Error: ${t.message}", t)
                }
            })
        }
    }
    // Fetch user profile from the server
    private fun getUserProfile(idToken: String) {
        apiService.getUserProfile(idToken).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    userProfile?.let {
                        // Set the username and photo from the API response
                        ed_signup_name.setText(it.username)
                        Glide.with(this@EditProfileActivity).load(it.photo).circleCrop()  .into(ivProfilePicture)
                    }
                } else {
                    Toast.makeText(this@EditProfileActivity, "Failed to load user profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Update username on the server
    private fun updateUserProfile(username: String) {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = "Bearer ${result.token}"
            val request = EditUserRequest(username, null)

            RetrofitInstance.api.editUser(idToken, request)
                .enqueue(object : Callback<EditUserResponse> {
                    override fun onResponse(call: Call<EditUserResponse>, response: Response<EditUserResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditProfileActivity, "Username updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@EditProfileActivity, "Failed to update username", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<EditUserResponse>, t: Throwable) {
                        Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }


    // Update profile photo with Base64 string
    private fun updateProfilePhotoBase64(base64String: String) {
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val idToken = "Bearer ${result.token}"
            val request = EditUserRequest(null, base64String)

            RetrofitInstance.api.editUser(idToken, request)
                .enqueue(object : Callback<EditUserResponse> {
                    override fun onResponse(call: Call<EditUserResponse>, response: Response<EditUserResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@EditProfileActivity, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this@EditProfileActivity, "Failed to update photo", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<EditUserResponse>, t: Throwable) {
                        Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // Convert image URI to Base64 string
    private fun convertImageToBase64(uri: Uri?): String? {
        try {
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                val byteArrayOutputStream = ByteArrayOutputStream()

                // Determine the image format based on the file extension
                val format = when {
                    uri.toString().endsWith("png") -> Bitmap.CompressFormat.PNG
                    uri.toString().endsWith("jpg") || uri.toString().endsWith("jpeg") -> Bitmap.CompressFormat.JPEG
                    else -> Bitmap.CompressFormat.JPEG  // Default to JPEG
                }

                // Compress the image to the chosen format
                bitmap.compress(format, 100, byteArrayOutputStream)

                val byteArray = byteArrayOutputStream.toByteArray()

                // Encode the byte array to Base64
                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                // Return the complete Base64 URL format with data:image/[format];base64,
                val imageType = when (format) {
                    Bitmap.CompressFormat.PNG -> "png"
                    Bitmap.CompressFormat.JPEG -> "jpeg"
                    else -> "jpg"  // Default to jpg for other formats
                }

                return "data:image/$imageType;base64,$base64String"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Handle the result from the image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the result is from the image picker
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.data // Get the URI of the selected photo

            // Use Glide to load the image and apply circleCrop
            Glide.with(this@EditProfileActivity)
                .load(selectedPhotoUri)
                .circleCrop() // Crop the image to be circular
                .into(ivProfilePicture) // Set the image to the ImageView
        }
    }

}


