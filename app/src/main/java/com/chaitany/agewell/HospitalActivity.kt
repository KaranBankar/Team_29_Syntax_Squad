package com.chaitany.agewell;
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

class HospitalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    lateinit var loadingdialog:LoadingDialog
    private lateinit var hospitalAdapter: HospitalAdapter
    private var hospitalList: MutableList<Hospital> = mutableListOf()

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    var userLat = 19.8762 // Example: Replace with actual user location
    var userLon = 75.3433

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)
        loadingdialog=LoadingDialog(this);
        loadingdialog.showLoadingDialog();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        hospitalAdapter = HospitalAdapter(hospitalList, userLat,userLon)
        recyclerView.adapter = hospitalAdapter

        // Initialize LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Initialize LocationListener to get location updates
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                userLat=latitude
                userLon=longitude
                // Fetch nearby hospitals using the current location
                getNearbyHospitals(latitude, longitude)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Request location permission and fetch the current location
        checkPermissionsAndFetchLocation()
    }

    // Check if location permissions are granted
    private fun checkPermissionsAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            // Request permission if not granted
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Launch permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permission denied. Can't fetch location.", Toast.LENGTH_SHORT).show()
        }
    }

    // Get current location
    private fun getCurrentLocation() {
        // Check if the device's location is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            // If location is disabled, prompt the user to enable it
            showToast("Location is disabled. Please enable location.")
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            return
        }

        // Request location updates from the best provider
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                provider ?: LocationManager.GPS_PROVIDER,
                60000, // 1-minute interval
                10f, // 10 meters minimum distance
                locationListener
            )
        }
    }

    // Fetch nearby hospitals based on the location
    private fun getNearbyHospitals(latitude: Double, longitude: Double) {
        val url = "https://nominatim.openstreetmap.org/search?q=hospitals+near+$latitude,$longitude&format=json&addressdetails=1&limit=10"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@HospitalActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    response.body?.let { body ->
                        try {
                            val jsonArray = JSONArray(body.string())
                            for (i in 0 until jsonArray.length()) {
                                val hospital = jsonArray.getJSONObject(i)
                                val hospitalName = hospital.getString("name")
                                val displayName = hospital.getString("display_name")
                                val lat = hospital.getDouble("lat")
                                val lon = hospital.getDouble("lon")
                                val placeId = hospital.optString("place_id", null)
                                val addressType = hospital.optString("addresstype", null)
                                val importance = hospital.optDouble("importance", 0.0)
                                val boundingBox = hospital.getJSONArray("boundingbox")
                                val boundingBoxList = mutableListOf<String>()
                                for (j in 0 until boundingBox.length()) {
                                    boundingBoxList.add(boundingBox.getString(j))
                                }

                                // Add hospital to list
                                val newHospital = Hospital(
                                    hospitalName,
                                    displayName,
                                    lat,
                                    lon,
                                    placeId,
                                    addressType,
                                    importance,
                                    boundingBoxList
                                )
                                hospitalList.add(newHospital)
                            }

                            // Update RecyclerView with new data on the main thread
                            runOnUiThread {
                                loadingdialog.dismissLoadingDialog();
                                hospitalAdapter.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(this@HospitalActivity, "Error parsing data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@HospitalActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }



    // Show toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Clean up the location listener when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.removeUpdates(locationListener)
        }
    }
}
