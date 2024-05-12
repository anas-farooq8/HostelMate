package com.and.hostelmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityMainBinding
import com.and.hostelmate.models.Accommodation
import com.and.hostelmate.models.MenuItem
import com.and.hostelmate.models.User
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException

interface UserDataCallback {
    fun onDataReady(user: User)
    fun onDataError(error: String)
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    companion object{
        const val IP: String = "192.168.100.6"
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        var database: FirebaseStorage = FirebaseStorage.getInstance()

        const val PREFS_NAME = "MyPrefsFile"
        const val LOGIN_STATUS_KEY = "loginStatus"
        const val PICK_IMAGE_REQUEST = 102
        const val REQUEST_STORAGE_PERMISSION = 101

        // make a user object
        var user: User = User()
        var accommodation: Accommodation = Accommodation()
        var menuItems: List<MenuItem> = emptyList()

        // Roles
        const val ROLE_STUDENT = "student"
        const val ROLE_ADMIN = "admin"
        const val ROLE_WARDEN = "warden"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isLoggedIn()) {
            // Navigate to LoginActivity
            navigateToLogin()
        } else {
            auth.currentUser?.uid?.let {
                fetchUserData(it, object : UserDataCallback {
                    override fun onDataReady(user: User) {
                        MainActivity.user = user
                        navigateToHomeActivity()
                    }

                    override fun onDataError(error: String) {
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, AdminDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000) // Delay to simulate loading/splash screen
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false) && auth.currentUser != null
    }

    private fun fetchUserData(userId: String, callback: UserDataCallback) {
        val url = "http://${IP}/api/getUserData.php?id=$userId"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val fetchedUser = User(
                        id = response.getString("ID"),
                        email = response.getString("email"),
                        role = response.getString("role"),
                        name = response.getString("name"),
                        age = response.getInt("age"),
                        cnic = response.getString("cnic"),
                        image = response.getString("image_address"),
                        phoneNo = response.getString("phone_no"),
                        homeAddress = response.getString("home_address"),
                        fatherCnic = response.getString("father_cnic"),
                        fatherPhoneNo = response.getString("father_phone_no")
                    )

                    // Get the bed ID from the response and set it in the User object
                    val bedId = response.optInt("bed_id", -1)
                    accommodation.bedId = bedId

                    // Fetch bed location if the user is a student
                    if (fetchedUser.role == ROLE_STUDENT && bedId != -1) {
                        fetchBedLocation(bedId, object : UserDataCallback {
                            override fun onDataReady(user: User) {
                                callback.onDataReady(fetchedUser)
                            }

                            override fun onDataError(error: String) {
                                Log.e("fetchUserData", "Error fetching bed location: $error")
                                callback.onDataError("Failed to fetch bed location")
                            }
                        })
                    } else {
                        callback.onDataReady(fetchedUser)
                    }
                } catch (e: Exception) {
                    Log.e("fetchUserData", "Error parsing user data", e)
                    callback.onDataError("Failed to parse user data")
                }
            },
            { error ->
                Log.e("fetchUserData", "Error: ${error.localizedMessage}")
                callback.onDataError("Network error: ${error.localizedMessage}")
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun fetchBedLocation(bedId: Int, callback: UserDataCallback) {
        val url = "http://${IP}/api/getBedLocation.php?bed_id=$bedId"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val block = response.getString("block")
                    val floor = response.getInt("floor")
                    val room = response.getInt("room")

                    // Store in the Accommodation object
                    accommodation.blockNo = block
                    accommodation.floorNo = floor
                    accommodation.roomNo = room

                    // Use block, floor, and room values as needed
                    Log.d("BedLocation", "Block: $block, Floor: $floor, Room: $room")

                    // Pass the fetched user object to onDataReady
                    callback.onDataReady(user)
                } catch (e: JSONException) {
                    Log.e("BedLocation", "Error parsing bed location data", e)
                    callback.onDataError("Failed to parse user data")
                }
            },
            { error ->
                Log.e("BedLocation", "Error: ${error.localizedMessage}")
                callback.onDataError("Network error: ${error.localizedMessage}")
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}