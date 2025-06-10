package com.chaitany.agewell

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaitany.agewell.R
import com.chaitany.agewell.com.chaitany.agewell.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class OTP: AppCompatActivity() {

    private lateinit var otpField: TextInputEditText
    private lateinit var verifyButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var database: DatabaseReference

    private lateinit var sentOtp: String
    private lateinit var mobile: String
    private lateinit var name: String
    private lateinit var age: String
    private lateinit var gender: String
    private lateinit var location: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        otpField = findViewById(R.id.otpField)
        verifyButton = findViewById(R.id.verifyButton)
        progressBar = findViewById(R.id.progressBar)

        // Get data passed from SignupActivity
        sentOtp = intent.getStringExtra("otp") ?: ""
        mobile = intent.getStringExtra("mobile") ?: ""
        name = intent.getStringExtra("name") ?: ""
        age = intent.getStringExtra("age") ?: ""
        gender = intent.getStringExtra("gender") ?: ""
        location = intent.getStringExtra("location") ?: ""
        password = intent.getStringExtra("password") ?: ""

        database = FirebaseDatabase.getInstance().reference

        verifyButton.setOnClickListener {
            val enteredOtp = otpField.text.toString()
            if (enteredOtp == sentOtp) {
                saveUserToDatabase()
            } else {
                Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToDatabase() {
        progressBar.visibility = View.VISIBLE
        val user = User(name, mobile, age, gender, location, password)

        val userRef = database.child("users").child(mobile)
        userRef.setValue(user).addOnCompleteListener { task ->
            progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                val intent=Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to register user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
