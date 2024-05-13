package com.and.hostelmate

import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.and.hostelmate.databinding.ActivityAvailableRoomBinding
import com.and.hostelmate.models.AvailRooms
import com.and.hostelmate.models.UserRequest
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class AvailableRoomActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AvailableRoomsAdapter
    private lateinit var binding: ActivityAvailableRoomBinding
    private var availRooms = mutableListOf<AvailRooms>()
    private lateinit var msg: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAvailableRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // set the tool bar to occupy the camera space too
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        toolbar.title = "Available Rooms"

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView1)


        fetchRoomData()

        binding.search.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Hide the keyboard
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.search.windowToken, 0)

                // Perform the search operation
                val searchText = textView.text.toString()
                fetchSearchData(searchText)
                true
            } else {
                false
            }
        }
    }

    private fun fetchSearchData(searchText: String) {
        val block = searchText.substring(0, 1)
        val floor = searchText.substring(1, 2).toInt()
        val roomNo = searchText.substring(2).toInt()



        // You can now use these values to search for matching rooms
        // Make sure to modify the URL and PHP accordingly
        val url = "http://${MainActivity.IP}/api/getSearchResult.php?block=$block&floor=$floor&roomNo=$roomNo"
        availRooms.clear() // Clear previous data
        val request = object : StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Parse room details
                        val availRoom = AvailRooms(
                            floorNo = jsonObject.getInt("floor"),
                            roomNo = jsonObject.getInt("room"),
                            block = jsonObject.getString("block"),
                            bed_id = jsonObject.getInt("bed_id")
                        )

                        availRooms.add(availRoom)

                    }
                    adapter.notifyDataSetChanged() // Notify adapter of data change

                } catch (e: Exception) {
                    e.printStackTrace()
                    adapter.notifyDataSetChanged() // Notify adapter of data change
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                msg = error.message.toString()
            }) {}

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchRoomData() {
        val url = "http://${MainActivity.IP}/api/getAllAvailRooms.php"

        val request = object : StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        // Parse user details
                        val avail_rooms = AvailRooms(
                            floorNo = jsonObject.getInt("floor"),
                            roomNo = jsonObject.getInt("room"),
                            block = jsonObject.getString("block"),
                            bed_id = jsonObject.getInt("bed_id")
                        )

                        availRooms.add(avail_rooms)

                    }

                    // Set adapter once data is fetched
                    adapter = AvailableRoomsAdapter(availRooms, this)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = adapter

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                msg = error.message.toString()
            }) {}

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request)
    }



    // Handles the Back Btn
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
