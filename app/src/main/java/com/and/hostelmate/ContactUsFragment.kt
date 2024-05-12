package com.and.hostelmate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.and.hostelmate.databinding.FragmentContactUsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
class ContactUsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentContactUsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        val view = binding.root

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle click on phone number
        val phoneNumberTextView = binding.phono
        val phoneNumberTextView1 = binding.phono1
        makeTextViewClickable(phoneNumberTextView, getString(R.string.phone_anas))
        makeTextViewClickable(phoneNumberTextView1, getString(R.string.phone_daniyal))


        // Handle click on email
        val emailTextView = binding.email
        val emailTextView1 = binding.email1
        makeTextViewClickable(emailTextView, getString(R.string.email_anas))
        makeTextViewClickable(emailTextView1, getString(R.string.email_daniyal))

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker and move the camera to the specified location with a higher zoom level
        val hostelLocation = LatLng(33.6711711, 72.9863258)
        mMap.addMarker(MarkerOptions().position(hostelLocation).title("Ghazali Hostel For Boys"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hostelLocation, 16.0f)) // Adjust the zoom level as needed
    }

    private fun makeTextViewClickable(textView: TextView, text: String) {
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle click action here
                if (textView.id == R.id.phono || textView.id == R.id.phono1) {
                    // If it's the phone number text view
                    dialPhoneNumber(text)
                } else if (textView.id == R.id.email || textView.id == R.id.email1) {
                    // If it's the email text view
                    sendEmail(text)
                }
            }
        }
        spannableString.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.green)) // Change text color to indicate clickable
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$email")
        startActivity(intent)
    }
}
