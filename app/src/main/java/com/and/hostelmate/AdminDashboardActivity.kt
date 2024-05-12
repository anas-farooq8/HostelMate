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
import com.squareup.picasso.Picasso
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

        Toast.makeText(this, MainActivity.user.name, Toast.LENGTH_SHORT).show()

        // set the tool bar to occupy the camera space too
        val toolbar = binding.toolbar
        toolbar.title = "Dashboard"

        val profileImageView = binding.profileImageView

        // Set OnClickListener to display a toast message when clicked
        profileImageView.setOnClickListener {
            // Proceed to the profile activity
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }


        // Set OnLongClickListener to display a toast message when held
        profileImageView.setOnLongClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
            true // return true to indicate that the long click event is consumed
        }

        loadDetails()

        // All the Activities
        binding.issues.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_STUDENT) {
                val intent = Intent(this, CreateIssueActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadDetails() {
        if(MainActivity.accomodation.bedId == -1) {
            return
        }

        binding.name.text = MainActivity.user.name

        val no = MainActivity.accomodation.floorNo.times(100)
            .plus(MainActivity.accomodation.roomNo)

        binding.roomNo.text = "Room No: ${MainActivity.accomodation.blockNo}-$no"
        binding.bedNo.text = "Bed Id: ${MainActivity.accomodation.bedId}"
        Picasso.get().load(MainActivity.user.image).into(binding.profileImage)

    }
}