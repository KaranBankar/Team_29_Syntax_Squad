package com.chaitany.agewell

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Splash : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserLogin", Context.MODE_PRIVATE)

        requestAllPermissions()  // Request all permissions at once


        // Setup window insets for edge-to-edge view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Request all required permissions
    private fun requestAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                permissions,
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            // Permissions already granted, check onboarding and login status
            checkOnboardingStatus()
        }
    }

    // Check if onboarding was shown, then check login status
    private fun checkOnboardingStatus() {
        val appPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstTime = appPreferences.getBoolean("isFirstTime", true)

        val handler = Handler()
        handler.postDelayed({
            val intent = if (isFirstTime) {
                // First time user, show OnBoardingActivity
                Intent(this, OnBoardingActivity::class.java)
            } else {
                checkLoginStatus() // Proceed with login check
                return@postDelayed
            }
            startActivity(intent)
            finish()
        }, 2000) // 2-second delay
    }

    // Check the login status from SharedPreferences
    private fun checkLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("isLogged", false)

        val intent = if (isLoggedIn) {
            // If user is logged in, navigate to the main activity/dashboard
            Intent(this, Dashboard::class.java)
        } else {
            // If not logged in, navigate to LoginActivity
            Intent(this, Login::class.java)
        }
        startActivity(intent)
        finish()
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted
                checkOnboardingStatus()
            } else {
                // Some permissions denied, show message to the user
                Toast.makeText(this, "Permissions are required to use the app", Toast.LENGTH_LONG).show()
                finish() // Forcibly close the splash activity
            }
        }
    }


}
