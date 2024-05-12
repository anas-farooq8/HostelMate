package com.and.hostelmate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityCreateStaffBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.util.UUID

class CreateStaffActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateStaffBinding
    private var imagePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.Add.setOnClickListener {
            val name = binding.Nametext.text.toString()
            val pass = binding.passText.text.toString()
            val email = binding.emailText.text.toString()
            val cnic = binding.cnic.text.toString()
            val age = binding.Age.text.toString()
            val phone = binding.PhoNo.text.toString()
            val address = binding.Address.text.toString()


            // validation check
            if(name.isEmpty()) {
                binding.Nametext.error = "Name is required"
                return@setOnClickListener
            }
            if(age.isEmpty()) {
                binding.Age.error = "Age is required"
                return@setOnClickListener
            }
            if(phone.isEmpty()) {
                binding.PhoNo.error = "Phone is required"
                return@setOnClickListener
            }
            if(address.isEmpty()) {
                binding.Address.error = "Address is required"
                return@setOnClickListener
            }
            if(cnic.isEmpty()) {
                binding.cnic.error = "CNIC is required"
                return@setOnClickListener
            }
            if(email.isEmpty()) {
                binding.emailText.error = "Email is required"
                return@setOnClickListener
            }
            if(pass.isEmpty()) {
                binding.passText.error = "Password is required"
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User created successfully, get the user ID (UID)
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid
                        Toast.makeText(applicationContext, userId.toString(), Toast.LENGTH_SHORT).show()

                        // Proceed with submitting the request or any other operation using userId
                        submitRequest(userId!!)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            applicationContext, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        binding.camera.setOnClickListener {
            if (isStoragePermissionGranted()) {
                openGallery()
            } else {
                checkStoragePermission()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, MainActivity.PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MainActivity.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data // URI of selected image

            imageUri?.let { uri ->
                val storageReference = MainActivity.database.getReference("users/user_request/${UUID.randomUUID()}.jpg")
                val uploadTask = storageReference.putFile(uri)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        imagePath = downloadUri.toString() // Save the download URL in the Realtime Database
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        loadProfileImage(imagePath!!)
                    } else {
                        // Handle failures
                        Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Handles the Back Btn
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadProfileImage(imagePath: String) {
        // if image path is not null, load the image using Picasso
        if (imagePath.isNotEmpty()) {
            Picasso.get().load(imagePath).into(binding.profileImageView)
        }
    }

    private fun submitRequest(userId: String) {
        val url = "http://${MainActivity.IP}/api/addStaff.php"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                Log.d("CreateStaffActivity", "Server Response: $response")
                // Notify user of success
                Toast.makeText(this, "Staff Added successfully", Toast.LENGTH_SHORT).show()
                finish()
            },
            Response.ErrorListener { error ->
                Log.e("CreateStaffActivity", "Error updating profile: ${error.message}")
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["ID"] = userId
                params["email"] = binding.emailText.text.toString()
                params["role"] = "warden"
                params["name"] = binding.Nametext.text.toString()
                params["age"] = binding.Age.text.toString()
                params["cnic"] = binding.cnic.text.toString()
                params["home_address"] = binding.Address.text.toString()
                params["phone_no"] = binding.PhoNo.text.toString()
                params["image_address"] = imagePath.toString()
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API level 33) and above, use READ_MEDIA_IMAGES for more specific access
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            // For older versions, check for READ_EXTERNAL_STORAGE permission
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API level 33) and above, use READ_MEDIA_IMAGES for more specific access
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), MainActivity.REQUEST_STORAGE_PERMISSION)
            }
        } else {
            // For older versions, check for READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MainActivity.REQUEST_STORAGE_PERMISSION)
            }
        }
    }
}