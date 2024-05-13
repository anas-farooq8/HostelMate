package com.and.hostelmate

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.and.hostelmate.models.AvailRooms
import com.and.hostelmate.models.UserRequest
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class AvailableRoomsAdapter(
    private var availRooms: List<AvailRooms>,
    private val context: AvailableRoomActivity
) : RecyclerView.Adapter<AvailableRoomsAdapter.AvailableRoomsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableRoomsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.available_room_item, parent, false)
        return AvailableRoomsViewHolder(view)
    }

    override fun getItemCount(): Int = availRooms.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AvailableRoomsViewHolder, position: Int) {
        val item = availRooms[position]

        holder.roomNo.text = item.roomNo.toString()
        holder.floorNo.text = item.floorNo.toString()
        holder.blocks.text = item.block
        holder.bed_id.text = item.bed_id.toString()

    }



    // View Holder Class
    class AvailableRoomsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var roomNo: TextView = itemView.findViewById(R.id.RoomnoText)
        var floorNo: TextView = itemView.findViewById(R.id.FloorNoText)
        var blocks: TextView = itemView.findViewById(R.id.blocks)
        var bed_id: TextView = itemView.findViewById(R.id.bed_id)

    }
}
