package com.chaitany.agewell

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chaitany.agewell.HospitalActivity
import com.chaitany.agewell.R

data class Hospital(
    val name: String,
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val placeId: String?,
    val addressType: String?,
    val importance: Double?,
    val boundingBox: List<String>?
)

class HospitalAdapter(
    private val hospitalList: List<Hospital>,
    private val userLat: Double, // User's latitude
    private val userLon: Double // User's longitude
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitalList[position]
        holder.nameTextView.text = hospital.name
        holder.displayNameTextView.text = hospital.displayName

        // Calculate and set the distance
        val distance = calculateDistance(userLat, userLon, hospital.lat, hospital.lon)
        holder.distanceTextView.text = " "

        // Open Google Maps Navigation
        holder.startNavigatingButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=${hospital.lat},${hospital.lon}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            holder.itemView.context.startActivity(mapIntent)
        }
    }

    override fun getItemCount(): Int = hospitalList.size

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)
        val displayNameTextView: TextView = itemView.findViewById(R.id.displayNameTextView)
        val startNavigatingButton: Button = itemView.findViewById(R.id.startNavigatingButton)
    }

    // Function to calculate distance in km
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        val distanceInKm = results[0] / 1000 // Convert meters to km
        return String.format("%.2f", distanceInKm) // Format to 2 decimal places
    }
}
