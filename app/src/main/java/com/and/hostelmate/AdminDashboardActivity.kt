package com.and.hostelmate

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityAdminDashboardBinding
import de.hdodenhof.circleimageview.CircleImageView

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


/*
        // set the tool bar to occupy the camera space too
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        toolbar.title = "Admin Dashboard"
        // toolbar.subtitle = "Hostel Mate"

        val profileImageView = binding.profileImageView

*/
/*        // Set OnClickListener to display a toast message when clicked
        profileImageView.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }*//*


        // Set OnLongClickListener to display a toast message when held
        profileImageView.setOnLongClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
            true // return true to indicate that the long click event is consumed
        }
*/

    }


}