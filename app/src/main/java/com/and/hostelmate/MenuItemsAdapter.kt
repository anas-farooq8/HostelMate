package com.and.hostelmate

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.and.hostelmate.models.MenuItem
import com.squareup.picasso.Picasso

class MenuItemsAdapter(private var menuItemsList: List<MenuItem>, private val context: FragmentActivity) : RecyclerView.Adapter<MenuItemsAdapter.MenuItemsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_item_card, parent, false)
        return MenuItemsViewHolder(view)
    }

    override fun getItemCount(): Int = menuItemsList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MenuItemsViewHolder, position: Int) {
        val menuItem = menuItemsList[position]
        holder.name.text = menuItem.name
        holder.mealType.text = menuItem.mealType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        holder.description.text = menuItem.description
        holder.rating.rating = menuItem.rating
        Picasso.get().load(menuItem.imageAddress).into(holder.image)

        holder.itemView.setOnLongClickListener {
            Toast.makeText(context, "Selected: ${menuItem.name}", Toast.LENGTH_SHORT).show()
            true // Indicate that the click was handled
        }
    }

    // Update the dataset of the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<MenuItem>) {
        Log.d("MenuItemsAdapter", "Updating items: ${newItems.size}")
        this.menuItemsList = newItems
        notifyDataSetChanged()
    }

    // View Holder Class
    class MenuItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.textViewMenuName)
        var description: TextView = itemView.findViewById(R.id.textViewDescription)
        var rating: RatingBar = itemView.findViewById(R.id.ratingBar)
        var image: ImageView = itemView.findViewById(R.id.imageViewMenu)
        var mealType: TextView = itemView.findViewById(R.id.textViewMealType)
    }
}
