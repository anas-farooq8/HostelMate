package com.and.hostelmate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityAdminDashboardBinding
import com.squareup.picasso.Picasso

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

        loadDetails()

        // set the tool bar to occupy the camera space too
        val toolbar = binding.toolbar
        toolbar.title = "Dashboard"

        // If the Role is Warden then show.
        if(MainActivity.user.role == MainActivity.ROLE_WARDEN) {
            binding.roomNo.text = "Warden"
        }

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

        // Notifications
        binding.notifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        // Chat Activity
        binding.chats.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // All the Activities
        // Available Rooms
        binding.availRooms.setOnClickListener {
            val intent = Intent(this, AvailableRoomActivity::class.java)
            startActivity(intent)
        }

        // Issues
        binding.issues.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_STUDENT) {
                val intent = Intent(this, CreateIssueActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, IssueListActivity::class.java)
                startActivity(intent)
            }
        }

        // Room Change Request
        binding.roomChange.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_STUDENT) {
                val intent = Intent(this, RoomChangeActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, RoomChangeRequestActivity::class.java)
                startActivity(intent)
            }
        }

        // Staff Members
        binding.createStaff.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_ADMIN) {
                val intent = Intent(this, CreateStaffActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Only Admin can access this.", Toast.LENGTH_SHORT).show()
            }
        }

        // Hostel Fees
        binding.hostelFees.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_STUDENT) {
                val intent = Intent(this, FeePayActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, FeeDueListActivity::class.java)
                startActivity(intent)
            }
        }

        // Staff Members
        binding.staff.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_ADMIN) {
                val intent = Intent(this, UsersListActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Only Admin can access this.", Toast.LENGTH_SHORT).show()
            }
        }

        // Add User from the Request
        binding.addMember.setOnClickListener {
            if(MainActivity.user.role == MainActivity.ROLE_ADMIN || MainActivity.user.role == MainActivity.ROLE_WARDEN) {
                val intent = Intent(this, AddStudentsActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Only Admin & Warden can access.", Toast.LENGTH_SHORT).show()
            }
        }

        // Menu
        binding.menu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDetails() {
        if(MainActivity.accommodation.bedId == -1 && MainActivity.user.role == MainActivity.ROLE_STUDENT) {
            return
        }

        binding.name.text = MainActivity.user.name
        Picasso.get().load(MainActivity.user.image).into(binding.profileImage)

        if (MainActivity.user.role == MainActivity.ROLE_STUDENT) {
            val no = MainActivity.accommodation.floorNo.times(100)
                .plus(MainActivity.accommodation.roomNo)

            binding.roomNo.text = "Room No: ${MainActivity.accommodation.blockNo}-$no"
            binding.bedNo.text = "Bed Id: ${MainActivity.accommodation.bedId}"
        }
    }

    override fun onResume() {
        super.onResume()
        loadDetails()
    }
}