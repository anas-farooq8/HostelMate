package com.and.hostelmate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityCreateIssueBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.UUID

class CreateIssueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateIssueBinding
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateIssueBinding.inflate(layoutInflater)
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

        toolbar.title = "Create Issue"


        // Upload Image (Optional)
        binding.uploadBtn.setOnClickListener {
            if (isStoragePermissionGranted()) {
                openGallery()
            } else {
                checkStoragePermission()
            }
        }

        binding.save.setOnClickListener {
            val title = binding.Problemtext.text.toString()
            val description = binding.problemDesc.text.toString()

            if(title.isEmpty()) {
                binding.Problemtext.error = "Age is required"
                return@setOnClickListener
            }
            if(description.isEmpty()) {
                binding.problemDesc.error = "Phone is required"
                return@setOnClickListener
            }

            createIssue(title, description, imagePath, MainActivity.user.id)
            finish()
        }
    }

    private fun createIssue(title: String, description: String, imageAddress: String?, userId: String) {
        val url = "http://${MainActivity.IP}/api/createIssue.php"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Handle the response
                Toast.makeText(this, "Issue Created Successfully.", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("CreateIssue", "Error: ${error.message}")
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["title"] = title
                params["description"] = description
                params["image_address"] = imageAddress ?: "" // Image address is optional
                params["user_id"] = userId
                return params
            }
        }
        requestQueue.add(stringRequest)
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
                val storageReference = MainActivity.database.getReference("users/issues/${UUID.randomUUID()}.jpg")
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
                        binding.fileName.text = "Selected..."
                    } else {
                        // Handle failures
                        Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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