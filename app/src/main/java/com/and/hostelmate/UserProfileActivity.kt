package com.and.hostelmate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityUserProfileBinding
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.and.hostelmate.models.User
import com.squareup.picasso.Picasso
import java.util.UUID

@Suppress("DEPRECATION")
class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // set the tool bar to occupy the camera space too
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        toolbar.title = "Profile"
        displayUserDetails()

        binding.save.setOnClickListener {
            val name = binding.Name.text.toString()
            val age = binding.Age.text.toString()
            val phone = binding.PhoNo.text.toString()
            val address = binding.Address.text.toString()

            // validation check
            if(name.isEmpty()) {
                binding.Name.error = "Name is required"
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
            updateProfile()
        }

        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
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
                val storageReference = MainActivity.database.getReference("users/profile_images/${UUID.randomUUID()}.jpg")
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

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    private fun displayUserDetails() {
        loadProfileImage(MainActivity.user.image!!)
        binding.Name.setText(MainActivity.user.name)
        binding.emailText.setText(MainActivity.user.email)
        binding.cnic.setText(MainActivity.user.cnic)
        binding.Age.setText(MainActivity.user.age.toString())
        binding.PhoNo.setText(MainActivity.user.phoneNo)
        binding.Address.setText(MainActivity.user.homeAddress)
        imagePath = MainActivity.user.image
    }

    private fun loadProfileImage(imagePath: String) {
        // if image path is not null, load the image using Picasso
        if (imagePath.isNotEmpty()) {
            Picasso.get().load(imagePath).into(binding.profileImageView)
        }
    }

    private fun updateProfile() {
        val url = "http://${MainActivity.IP}/api/updateUserData.php"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                Log.d("UserProfileActivity", "Server Response: $response")
                // Update local user object on successful update
                MainActivity.user.apply {
                    name = binding.Name.text.toString()
                    phoneNo = binding.PhoNo.text.toString()
                    homeAddress = binding.Address.text.toString()
                    age = binding.Age.text.toString().toInt()
                    image = imagePath  // Update the image in the user object
                }
                // Notify user of success
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            },
            Response.ErrorListener { error ->
                Log.e("UserProfileActivity", "Error updating profile: ${error.message}")
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["ID"] = MainActivity.user.id
                params["name"] = binding.Name.text.toString()
                params["phone_no"] = binding.PhoNo.text.toString()
                params["home_address"] = binding.Address.text.toString()
                params["age"] = binding.Age.text.toString()
                params["image_address"] = imagePath.toString()
                return params
            }
        }
        queue.add(stringRequest)
    }


    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Logout") { _, _ ->
            MainActivity.auth.signOut()
            val sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(MainActivity.LOGIN_STATUS_KEY, false).apply()

            // Resetting the user
            MainActivity.user = User()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            openGallery()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
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