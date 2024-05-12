package com.and.hostelmate

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.hostelmate.databinding.ActivityFeePayBinding
import com.squareup.picasso.Picasso

class FeePayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeePayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeePayBinding.inflate(layoutInflater)
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

        toolbar.title = "User's Fees"

        binding.t2.text = MainActivity.user.name
        val no = MainActivity.accommodation.floorNo.times(100)
            .plus(MainActivity.accommodation.roomNo)
        binding.t4.text = no.toString()
        binding.t6.text = MainActivity.accommodation.blockNo
        binding.t8.text = MainActivity.user.email
        binding.t10.text = MainActivity.user.phoneNo

        Picasso.get().load(MainActivity.user.image).into(binding.imageView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}