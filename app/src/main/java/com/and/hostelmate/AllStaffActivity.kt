package com.and.hostelmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.and.hostelmate.databinding.ActivityAvailableRoomBinding
import com.and.hostelmate.models.AvailRooms
import com.and.hostelmate.models.User
import com.and.hostelmate.models.UserRequest
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class AllStaffActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllStaffAdapter
    val userList = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_staff)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView2)

        fetchUserData()

    }


    private fun fetchUserData() {
        val url = "http://${MainActivity.IP}/api/getAllStaff.php"

        val request = object : StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)


                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Parse user details
                        val user = User(

                            name = jsonObject.getString("name"),
                            email = jsonObject.getString("email"),
                            phoneNo = jsonObject.getString("phone_no"),
                            role = jsonObject.getString("role"),
                            cnic = jsonObject.getString("cnic"),
                            age = jsonObject.getInt("age"),
                            image = jsonObject.getString("image_address"),
                            id = jsonObject.getString("ID")

                        )

                        userList.add(user)
                    }

                    adapter = AllStaffAdapter(userList, this)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = adapter




                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {}

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request)
    }



}