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
import com.and.hostelmate.models.User
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
import org.json.JSONArray
import org.json.JSONObject



class AllStaffAdapter(
    private var users: List<User>,
    private val context: AllStaffActivity
) : RecyclerView.Adapter<AllStaffAdapter.AllStaffViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllStaffViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.all_staff_item, parent, false)
        return AllStaffViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AllStaffViewHolder, position: Int) {
        val item = users[position]
        holder.name.text = item.name
        holder.email.text = item.email
        holder.phone.text = item.phoneNo
        holder.role.text = item.role
        holder.cnic.text = item.cnic
        holder.age.text = item.age.toString()
        Picasso.get().load(item.image).into(holder.profileImage)

        holder.delete.setOnClickListener {
            deleteUser(item.id)
            notifyDataSetChanged()

        }


    }


    private fun deleteUser(userId: String) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Delete user from database
                        val db = FirebaseFirestore.getInstance()

                        Log.d(TAG, "User authentication deleted successfully.")
                        //fetchSearchData(userId)

                    } else {
                        Log.w(TAG, "Error deleting user authentication:", task.exception)
                    }
                }
        } else {
            Log.w(TAG, "No user currently signed in.")
        }
    }

//    private fun removeRequest(item: UserRequest) {
//        val url = "http://${MainActivity.IP}/api/deleteUserRequest.php"
//        val queue = Volley.newRequestQueue(context)
//
//        val stringRequest = object : StringRequest(
//            Method.POST, url,
//            Response.Listener { _ ->
//                // Display success message
//                Toast.makeText(context, "Admission request removed", Toast.LENGTH_SHORT).show()
//                // Remove the item from the lists
//                val position = userRequests.indexOf(item)
//                if (position != -1) {
//                    userRequests = userRequests.toMutableList().apply { removeAt(position) }
//                    assignRooms = assignRooms.toMutableList().apply { removeAt(position) }
//                    // Notify the adapter that the item has been removed
//                    notifyItemRemoved(position)
//                }
//            },
//            Response.ErrorListener { error ->
//                Log.e("SignUp", "Error removing admission request: ${error.message}")
//                Toast.makeText(context, "Error removing admission request: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        ) {
//            override fun getParams(): Map<String, String> {
//                val params = HashMap<String, String>()
//                params["ID"] = item.id.toString()
//                return params
//            }
//        }
//        queue.add(stringRequest)
//    }


    // Example usage of the delete user function
    // View Holder Class
    class AllStaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.t2)
        var email: TextView = itemView.findViewById(R.id.t4)
        var phone: TextView = itemView.findViewById(R.id.t10)
        var role: TextView = itemView.findViewById(R.id.tt2)
        var profileImage: CircleImageView = itemView.findViewById(R.id.imageView)
        var cnic: TextView = itemView.findViewById(R.id.t6)
        var age : TextView = itemView.findViewById(R.id.t8)
        var delete: Button = itemView.findViewById(R.id.delBtn)

    }
}
