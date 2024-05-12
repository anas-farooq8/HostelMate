package com.and.hostelmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityMainBinding
import com.and.hostelmate.models.MenuItem
import com.and.hostelmate.models.User
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

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
        var menuItems: List<MenuItem> = emptyList()

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
                    callback.onDataReady(fetchedUser)
                } catch (e: Exception) {
                    Log.e("UserData", "Error parsing user data", e)
                    callback.onDataError("Failed to parse user data")
                }
            },
            { error ->
                Log.e("UserData", "Error: ${error.localizedMessage}")
                callback.onDataError("Network error: ${error.localizedMessage}")
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

}