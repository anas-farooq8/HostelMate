package com.and.hostelmate

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.and.hostelmate.databinding.ActivityLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email or password is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        val url = "http://${Singular.IP}/api/login.php"
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url,
            JSONObject(params as MutableMap<*, *>),
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")
                    if (success) {
                        // Login successful, navigate to the dashboard
                        val intent = Intent(this, AdminDashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Login failed
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // JSON parsing error
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "Response: $response")
            },
            { error ->
                // Volley error
                Log.e(TAG, "Volley Error: ${error.message}")
                Toast.makeText(this, "Volley Error: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}
