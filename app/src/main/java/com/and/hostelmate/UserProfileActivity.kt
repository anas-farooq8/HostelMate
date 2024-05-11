package com.and.hostelmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import java.io.ByteArrayOutputStream

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private var imageUri: Uri? = null
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
            checkStoragePermission()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, MainActivity.PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == MainActivity.PICK_IMAGE_REQUEST) {
            imageUri = data?.data
            binding.profileImageView.setImageURI(imageUri)  // Set image directly or use Picasso for more options

            // Using Picasso to load
            Picasso.get().load(imageUri).into(binding.profileImageView)
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
        loadProfileImage()
        binding.Name.setText(MainActivity.user.name)
        binding.emailText.setText(MainActivity.user.email)
        binding.cnic.setText(MainActivity.user.cnic)
        binding.Age.setText(MainActivity.user.age.toString())
        binding.PhoNo.setText(MainActivity.user.phoneNo)
        binding.Address.setText(MainActivity.user.homeAddress)
    }

    private fun loadProfileImage() {
/*        Picasso.get()
            .load(MainActivity.user.image)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.circled_background)
            .into(binding.profileImageView)*/
    }

    private fun updateProfile() {
        val url = "http://${MainActivity.IP}/api/updateUserData.php"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { _ ->
                MainActivity.user.apply {
                    name = binding.Name.text.toString()
                    phoneNo = binding.PhoNo.text.toString()
                    homeAddress = binding.Address.text.toString()
                    age = binding.Age.text.toString().toInt()
                }
                // Display success message
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                // Finish the current activity
                finish()
            },
            Response.ErrorListener { error ->
                Log.e("UpdateProfile", "Error updating profile: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["ID"] = MainActivity.user.id
                params["name"] = binding.Name.text.toString()
                params["phone_no"] = binding.PhoNo.text.toString()
                params["home_address"] = binding.Address.text.toString()
                params["age"] = binding.Age.text.toString()
                val imageString = encodeImageToBase64(imageUri!!)  // Make sure to define imageUri properly where you handle onActivityResult
                params["image"] = imageString
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

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }

    private fun encodeImageToBase64(imageUri: Uri): String {
        val bitmap = getBitmapFromUri(imageUri)
        ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
            val byteArray = this.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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