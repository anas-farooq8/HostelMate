package com.and.hostelmate

import android.annotation.SuppressLint
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
import com.and.hostelmate.models.UserRequest
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject

@Suppress("NAME_SHADOWING")
class AdmissionRequestAdapter(
    private var userRequests: List<UserRequest>,
    private var assignRooms: List<String>,
    private val context: AddStudentsActivity
) : RecyclerView.Adapter<AdmissionRequestAdapter.AdmissionRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdmissionRequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.addmission_request_recycler_view, parent, false)
        return AdmissionRequestViewHolder(view)
    }

    override fun getItemCount(): Int = userRequests.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AdmissionRequestViewHolder, position: Int) {
        val item = userRequests[position]
        val assignRoom = assignRooms[position] // Renamed to assignRoom

        holder.name.text = item.name
        holder.email.text = item.email
        holder.cnic.text = item.cnic
        holder.fatherCnic.text = item.fatherCNIC
        holder.phoneNo.text = item.phone
        holder.preferredRoom.text = item.preferRoom.toString()
        Picasso.get().load(item.image).into(holder.image)

        // Split the assignRoom string into individual items
        val assignRoomList = assignRoom.split(", ")

        // Set up the Spinner adapter with the list of assignRoom strings
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, assignRoomList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.assignRoom.adapter = spinnerAdapter

        holder.acceptButton.setOnClickListener {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(item.email, item.pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User created successfully, get the user ID (UID)
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid

                        // Proceed with submitting the request or any other operation using userId
                        val item = userRequests[position]
                        val selectedItem = holder.assignRoom.selectedItem.toString()

                        // A-201(21) get the bed id inside the ()
                        val bedId = selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(")"))

                        if (userId != null) {
                            submitRequest(item, bedId,userId)
                        }
                        else{
                            Toast.makeText(context, "User ID is null", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            context, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        holder.rejectButton.setOnClickListener {
            val item = userRequests[position]
            removeRequest(item)
        }
    }

    private fun submitRequest(item: UserRequest, bedId: String, userId: String) {
        val url = "http://${MainActivity.IP}/api/adduser.php"
        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getBoolean("success")
                    val message = jsonObject.getString("message")
                    if (success) {
                        // Display success message
                        Toast.makeText(context, "Admission request accepted", Toast.LENGTH_SHORT).show()
                        // Remove the item from the lists
                        val position = userRequests.indexOf(item)
                        if (position != -1) {
                            userRequests = userRequests.toMutableList().apply { removeAt(position) }
                            assignRooms = assignRooms.toMutableList().apply { removeAt(position) }
                            // Notify the adapter that the item has been removed
                            notifyItemRemoved(position)
                        }
                    } else {
                        Log.e("SignUp", "Error Requesting Admission: $message")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Log.e("SignUp", "Error Requesting Admission: ${error.message}")
                Toast.makeText(context, "Error Requesting Admission: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["ID"] = userId
                params["email"] = item.email
                params["password"] = item.pass
                params["name"] = item.name
                params["age"] = item.age
                params["cnic"] = item.cnic
                params["home_address"] = item.address
                params["phone_no"] = item.phone
                params["prefer_room"] = item.preferRoom.toString()
                params["image_address"] = item.image.toString()
                params["father_cnic"] = item.fatherCNIC
                params["father_phone_no"] = item.fatherPhoneNo
                params["bed_id"] = bedId
                params["role"] = "student"
                params["userRequests_id"] = item.id.toString()
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun removeRequest(item: UserRequest) {
        val url = "http://${MainActivity.IP}/api/deleteUserRequest.php"
        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { _ ->
                // Display success message
                Toast.makeText(context, "Admission request removed", Toast.LENGTH_SHORT).show()
                // Remove the item from the lists
                val position = userRequests.indexOf(item)
                if (position != -1) {
                    userRequests = userRequests.toMutableList().apply { removeAt(position) }
                    assignRooms = assignRooms.toMutableList().apply { removeAt(position) }
                    // Notify the adapter that the item has been removed
                    notifyItemRemoved(position)
                }
            },
            Response.ErrorListener { error ->
                Log.e("SignUp", "Error removing admission request: ${error.message}")
                Toast.makeText(context, "Error removing admission request: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["ID"] = item.id.toString()
                return params
            }
        }
        queue.add(stringRequest)
    }

    // View Holder Class
    class AdmissionRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.t2)
        var email: TextView = itemView.findViewById(R.id.t4)
        var cnic: TextView = itemView.findViewById(R.id.t6)
        var fatherCnic: TextView = itemView.findViewById(R.id.t8)
        var phoneNo: TextView = itemView.findViewById(R.id.t10)
        var preferredRoom: TextView = itemView.findViewById(R.id.tt2)
        var assignRoom: Spinner = itemView.findViewById(R.id.RoomAssign)
        var acceptButton: Button = itemView.findViewById(R.id.accept)
        var rejectButton: Button = itemView.findViewById(R.id.reject)
        var image: CircleImageView = itemView.findViewById(R.id.imageView)
    }
}
