package com.and.hostelmate

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.and.hostelmate.databinding.ActivityGuestBinding
import com.google.android.material.navigation.NavigationView

@Suppress("DEPRECATION")
class GuestActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityGuestBinding
    private lateinit var drawerLayout: DrawerLayout

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        // set the tool bar to occupy the camera space too
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // set the navigation icon to open the drawer
        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        // set the drawer layout to open and close on the navigation icon
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // set the default fragment to the menu fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            binding.navView.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_home -> replaceFragment(HomeFragment())
            R.id.nav_menu-> replaceFragment(MenuFragment())
            R.id.nav_add_request -> replaceFragment(SignUpFragment())
            R.id.nav_contact -> replaceFragment(ContactUsFragment())
            R.id.nav_login -> finish()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}