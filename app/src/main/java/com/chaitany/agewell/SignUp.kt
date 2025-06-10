package com.chaitany.agewell

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaitany.agewell.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class SignUp : AppCompatActivity() {

    private lateinit var nameField: TextInputEditText
    private lateinit var mobileField: TextInputEditText
    private lateinit var ageField: TextInputEditText
    private lateinit var genderField: AutoCompleteTextView
    private lateinit var locationField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var signupButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton:MaterialButton
    private val REQUEST_CODE_SEND_SMS = 101
    private lateinit var genderOptions:ArrayAdapter<String>

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize views
        nameField = findViewById(R.id.nameField)
        mobileField = findViewById(R.id.mobileField)
        ageField = findViewById(R.id.ageField)
        genderField = findViewById(R.id.genderField)
        locationField = findViewById(R.id.locationField)
        passwordField = findViewById(R.id.passwordField)
        signupButton = findViewById(R.id.signupButton)
        progressBar = findViewById(R.id.progressBar)
        loginButton=findViewById(R.id.btnLogin)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val cbShowPassword = findViewById<CheckBox>(R.id.cbShowPassword)

        val genderOptions = arrayOf("Male", "Female", "Other")

// Create an adapter and set it to AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        genderField.setAdapter(adapter)
        genderField.setOnClickListener {
            genderField.showDropDown()
        }

        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            togglePasswordVisibility(passwordField, isChecked)
        }
        signupButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val mobile = mobileField.text.toString().trim()
            val age = ageField.text.toString().trim()
            val gender = genderField.text.toString().trim()
            val location = locationField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateFields(nameField, mobileField, ageField, genderField, locationField, passwordField, progressBar)) {
                progressBar.visibility = View.VISIBLE
                checkIfUserExists(mobile) { exists ->
                    if (exists) {
                        progressBar.visibility = View.GONE
                        showUserExistsDialog(mobile) // Show alert if user exists
                    } else {
                        val otp = generateOtp()
                        sendOtpToUser(mobile, otp) // Send OTP to user
                        openOtpActivity(mobile, otp, name, age, gender, location, password) // Open OTP activity
                    }
                }
            }
        }


        loginButton.setOnClickListener {
            val intent=Intent(this,Login::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun generateOtp(): String {
        // Generate a random 6-digit OTP
        return Random.nextInt(100000, 999999).toString()
    }

    private fun sendOtpToUser(mobile: String, otp: String) {
        // Check if the permission is granted for SEND_SMS
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is granted, proceed to send OTP
            sendSms(mobile, otp)
        } else {
            // If permission is not granted, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.SEND_SMS),
                REQUEST_CODE_SEND_SMS
            )
        }
    }

    private fun sendSms(mobile: String, otp: String) {
        try {
            // Create the message to send
            val message = "Your OTP is: $otp"

            // Send SMS using SmsManager
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(mobile, null, message, null, null)

            // Show toast to notify the user that OTP has been sent
            Toast.makeText(this, "OTP sent to $mobile", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle exception in case of errors
            Toast.makeText(this, "Failed to send OTP: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If permission granted, send OTP
                    val mobile = mobileField.text.toString()
                    val otp = generateOtp()
                    sendOtpToUser(mobile, otp)
                } else {
                    // If permission denied, show a message
                    Toast.makeText(this, "Permission to send SMS is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openOtpActivity(
        mobile: String,
        otp: String,
        name: String,
        age: String,
        gender: String,
        location: String,
        password: String
    ) {
        // Intent to start the OTP activity where the user will enter the OTP
        val intent = Intent(this, OTP::class.java).apply {
            putExtra("otp", otp)
            putExtra("mobile", mobile)
            putExtra("name", name)
            putExtra("age", age)
            putExtra("gender", gender)
            putExtra("location", location)
            putExtra("password", password)
        }
        startActivity(intent)
        finish()
    }

    private fun validateFields(
        nameField: TextInputEditText,
        mobileField: TextInputEditText,
        ageField: TextInputEditText,
        genderField: AutoCompleteTextView,
        locationField: TextInputEditText,
        passwordField: TextInputEditText,
        progressBar: ProgressBar
    ): Boolean {
        var isValid = true

        // Reset previous errors
        nameField.error = null
        mobileField.error = null
        ageField.error = null
        genderField.error = null
        locationField.error = null
        passwordField.error = null

        val name = nameField.text.toString().trim()
        val mobile = mobileField.text.toString().trim()
        val age = ageField.text.toString().trim()
        val gender = genderField.text.toString().trim()
        val location = locationField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        // Validate Name
        if (name.isEmpty()) {
            nameField.error = "Name cannot be empty"
            isValid = false
        }

        // Validate Mobile Number
        if (mobile.isEmpty()) {
            mobileField.error = "Mobile number cannot be empty"
            isValid = false
        } else if (mobile.length != 10 || !mobile.matches(Regex("^[0-9]{10}$"))) {
            mobileField.error = "Enter a valid 10-digit mobile number"
            isValid = false
        }

        // Validate Age
        if (age.isEmpty()) {
            ageField.error = "Age cannot be empty"
            isValid = false
        } else {
            try {
                val ageInt = age.toInt()
                if (ageInt <= 0) {
                    ageField.error = "Enter a valid age"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                ageField.error = "Age must be a number"
                isValid = false
            }
        }

        // Validate Gender
        if (gender.isEmpty()) {
            findViewById<TextInputLayout>(R.id.genderInputLayout).error = "Select a valid gender"
            isValid=false
        } else {
            findViewById<TextInputLayout>(R.id.genderInputLayout).error = null
        }

        // Validate Location
        if (location.isEmpty()) {
            locationField.error = "Location cannot be empty"
            isValid = false
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordField.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            passwordField.error = "Password must be at least 6 characters long"
            isValid = false
        }

        if (!isValid) {
            progressBar.visibility = View.GONE
        }

        return isValid
    }


    private fun togglePasswordVisibility(passwordField: TextInputEditText, showPassword: Boolean) {
        if (showPassword) {
            passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        passwordField.setSelection(passwordField.text?.length ?: 0) // Keeps cursor at the end
    }

    private fun checkIfUserExists(mobile: String, callback: (Boolean) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        databaseRef.child(mobile).get().addOnSuccessListener { dataSnapshot ->
            callback(dataSnapshot.exists()) // Returns true if user exists, false otherwise
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Error checking user", Toast.LENGTH_SHORT).show()
            callback(false) // Assume false in case of error
        }
    }

    private fun showUserExistsDialog(mobile: String) {
        AlertDialog.Builder(this)
            .setTitle("User Already Exists")
            .setMessage("An account is already registered with this mobile number: $mobile.\nPlease signup in or use a different number.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
