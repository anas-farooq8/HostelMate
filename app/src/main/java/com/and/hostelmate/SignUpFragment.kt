package com.and.hostelmate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.and.hostelmate.databinding.FragmentSignUpBinding
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import java.util.UUID

@Suppress("DEPRECATION")
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var imagePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root

        val spinner: Spinner = binding.NoBeds
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.bed_numbers_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.submitRequest.setOnClickListener {
            val name = binding.Nametext.text.toString()
            val pass = binding.passText.text.toString()
            val email = binding.emailText.text.toString()
            val cnic = binding.cnic.text.toString()
            val age = binding.Age.text.toString()
            val noBeds = binding.NoBeds.selectedItem.toString()
            val phone = binding.PhoNo.text.toString()
            val address = binding.Address.text.toString()
            val fatherCNIC = binding.fatherCnic1.text.toString()
            val fatherPhone = binding.fatherPhno.text.toString()

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
            if(fatherCNIC.isEmpty()) {
                binding.fatherCnic1.error = "Father CNIC is required"
                return@setOnClickListener
            }
            if(fatherPhone.isEmpty()) {
                binding.fatherPhno.error = "Father Phone is required"
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
            if(noBeds.isEmpty()) {
                Toast.makeText(requireContext(), "Please select number of beds", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRequest()
        }

        binding.camera.setOnClickListener {
            if (isStoragePermissionGranted()) {
                openGallery()
            } else {
                checkStoragePermission()
            }
        }

        return view
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
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        loadProfileImage(imagePath!!)
                    } else {
                        // Handle failures
                        Toast.makeText(requireContext(), "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadProfileImage(imagePath: String) {
        // if image path is not null, load the image using Picasso
        if (imagePath.isNotEmpty()) {
            Picasso.get().load(imagePath).into(binding.profileImageView)
        }
    }

    private fun submitRequest() {
        val url = "http://${MainActivity.IP}/api/userRequest.php"
        val queue = Volley.newRequestQueue(requireContext())

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { _ ->
                // Display success message
                Toast.makeText(requireContext(), "Admission Successfully Requested", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()

            },
            Response.ErrorListener { error ->
                Log.e("SignUp", "Error Requesting Admission: ${error.message}")
                Toast.makeText(requireContext(), "Error Requesting Admission: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = binding.emailText.text.toString()
                params["password"] = binding.passText.text.toString()
                params["name"] = binding.Nametext.text.toString()
                params["age"] = binding.Age.text.toString()
                params["cnic"] = binding.cnic.text.toString()
                params["home_address"] = binding.Address.text.toString()
                params["phone"] = binding.PhoNo.text.toString()
                params["prefer_room"] = binding.NoBeds.selectedItem.toString()
                params["image_address"] = imagePath.toString()
                params["father_cnic"] = binding.fatherCnic1.text.toString()
                params["father_phone_no"] = binding.fatherPhno.text.toString()
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API level 33) and above, use READ_MEDIA_IMAGES for more specific access
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            // For older versions, check for READ_EXTERNAL_STORAGE permission
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API level 33) and above, use READ_MEDIA_IMAGES for more specific access
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_IMAGES), MainActivity.REQUEST_STORAGE_PERMISSION)
            }
        } else {
            // For older versions, check for READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MainActivity.REQUEST_STORAGE_PERMISSION)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}