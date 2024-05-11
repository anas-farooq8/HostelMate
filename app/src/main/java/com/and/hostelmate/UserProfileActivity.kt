package com.and.hostelmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityUserProfileBinding
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import android.util.Log

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
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
        binding.profileImageView.setImageResource(R.drawable.create_staff)

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
    }

    private fun displayUserDetails() {
        binding.Name.text = MainActivity.user.name
        binding.emailText.setText(MainActivity.user.email)
        binding.cnic.setText(MainActivity.user.cnic)
        binding.Age.setText(MainActivity.user.age.toString())
        binding.PhoNo.setText(MainActivity.user.phoneNo)
        binding.Address.setText(MainActivity.user.homeAddress)
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
                return params
            }
        }
        queue.add(stringRequest)
    }
}