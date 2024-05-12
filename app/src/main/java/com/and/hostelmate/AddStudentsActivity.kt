package com.and.hostelmate

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.and.hostelmate.databinding.ActivityAddStudentsBinding
import com.and.hostelmate.models.UserRequest
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class AddStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStudentsBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdmissionRequestAdapter
    private var userRequests = mutableListOf<UserRequest>()
    private var assignRooms = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentsBinding.inflate(layoutInflater)
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

        toolbar.title = "Admission Request"

        // Fetch user details and room information from PHP script
        fetchUserData()
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

    private fun fetchUserData() {
        val url = "http://${MainActivity.IP}/api/getAdmissionData.php"

        val request = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)
                    if(jsonArray.length() == 0) {
                        Toast.makeText(this, "No admission requests found", Toast.LENGTH_SHORT).show()
                        return@Listener
                    }

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Parse user details
                        val userRequest = UserRequest(
                            id = jsonObject.getInt("ID"),
                            name = jsonObject.getString("name"),
                            pass = jsonObject.getString("password"),
                            email = jsonObject.getString("email"),
                            cnic = jsonObject.getString("cnic"),
                            age = jsonObject.getString("age"),
                            preferRoom = jsonObject.getInt("prefer_room"),
                            phone = jsonObject.getString("phone_no"),
                            address = jsonObject.getString("home_address"),
                            fatherCNIC = jsonObject.getString("father_cnic"),
                            fatherPhoneNo = jsonObject.getString("father_phone_no"),
                            image = jsonObject.getString("image_address")
                        )

                        userRequests.add(userRequest)

                        // Parse concatenated room information
                        val roomsConcatenated = jsonObject.getString("rooms_concatenated")
                        assignRooms.add(roomsConcatenated)
                    }

                    // Initialize RecyclerView and adapter after fetching data
                    initializeRecyclerView()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("AddStudentsActivity", "Error parsing JSON: ${e.localizedMessage}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("AddStudentsActivity", "Error: ${error.localizedMessage}")
            }) {}

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request)
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        adapter = AdmissionRequestAdapter(userRequests, assignRooms, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

}
