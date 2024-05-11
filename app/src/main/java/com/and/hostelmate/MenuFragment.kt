package com.and.hostelmate

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.and.hostelmate.databinding.FragmentMenuBinding
import com.and.hostelmate.models.MenuItem
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import com.android.volley.Request
import org.json.JSONArray

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var menuItemsAdapter: MenuItemsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        fetchMenuItems()
    }

    private fun setupViews() {
        // Assume MainActivity is handling the toolbar
        menuItemsAdapter = MenuItemsAdapter(MainActivity.menuItems, requireActivity())
        binding.recyclerViewMenu.layoutManager = LinearLayoutManager(context)
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
                    updateRecyclerView("Monday")  // Update to display Monday's menu by default
                } catch (e: JSONException) {
                    Log.e("MenuFragment", "Error parsing menu items", e)
                }
            },
            { error ->
                Log.e("MenuFragment", "Error: ${error.localizedMessage}")
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonArrayRequest)
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}