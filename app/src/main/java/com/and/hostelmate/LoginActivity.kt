package com.and.hostelmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.and.hostelmate.databinding.ActivityLoginBinding
import com.and.hostelmate.models.User
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            if(email.isEmpty()) {
                binding.emailText.error = "Email is required"
                return@setOnClickListener
            }
            if(password.isEmpty()) {
                binding.passwordText.error = "Password is required"
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        // Continue as a Guest
        binding.continueAsAGuest.setOnClickListener {
            val intent = Intent(this@LoginActivity, GuestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        MainActivity.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(MainActivity.LOGIN_STATUS_KEY, true).apply()

                Toast.makeText(this, "Logged in as ${MainActivity.auth.currentUser?.uid}", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

}
