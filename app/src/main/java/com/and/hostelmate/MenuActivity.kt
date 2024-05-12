package com.and.hostelmate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.and.hostelmate.databinding.ActivityMenuBinding
import com.and.hostelmate.models.MenuItem
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONArray
import org.json.JSONException

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var menuItemsAdapter: MenuItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchMenuItems()
        setupViews()
    }

    private fun setupViews() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.title = "Menu"

        menuItemsAdapter = MenuItemsAdapter(MainActivity.menuItems, this)
        binding.recyclerViewMenu.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMenu.adapter = menuItemsAdapter

        binding.spinnerDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedDay = parent.getItemAtPosition(position) as String
                updateRecyclerView(selectedDay)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchMenuItems() {
        val url = "http://${MainActivity.IP}/api/getMenuItems.php"
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    MainActivity.menuItems = parseMenuItems(response)
                    // Should only display the monday's menu by default
                    updateRecyclerView("Monday")
                } catch (e: JSONException) {
                    Log.e("MenuActivity", "Error parsing menu items", e)
                }
            },
            { error ->
                Log.e("MenuActivity", "Error: ${error.localizedMessage}")
            }
        )
        Volley.newRequestQueue(this).add(jsonArrayRequest)
    }

    // Parse the JSON response and return a list of MenuItems
    private fun parseMenuItems(jsonArray: JSONArray): List<MenuItem> {
        val itemsList = mutableListOf<MenuItem>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            itemsList.add(
                MenuItem(
                    menuId = item.getInt("menu_id"),
                    name = item.getString("name"),
                    description = item.getString("description"),
                    rating = item.getDouble("rating").toFloat(),
                    imageAddress = item.optString("image_address"), // Use optString to handle null
                    day = item.getString("day_name"),
                    mealType = item.getString("meal_type")
                )
            )
        }
        return itemsList
    }

    private fun updateRecyclerView(selectedDay: String) {
        val filteredItems = MainActivity.menuItems.filter { it.day == selectedDay }
        menuItemsAdapter.updateItems(filteredItems)
    }

    // Handles the Back Btn
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}