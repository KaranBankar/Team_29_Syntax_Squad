package com.chaitany.agewell

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yourapp.utils.UserPreferences
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var mobileEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: Button
    private lateinit var progressDialog: ProgressDialog

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        mobileEditText = findViewById(R.id.etMobileNumber)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLoginNow)
        signUpButton = findViewById(R.id.btnSignUp)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Logging in...")
        progressDialog.setCancelable(false)

        // Login button click listener
        loginButton.setOnClickListener {
            val mobile = mobileEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateLoginFields(mobileEditText, passwordEditText)) {
                // Show ProgressDialog
                progressDialog.show()

                // Call login function
                loginUser(mobile, password)
            }
        }

        // Sign up button click listener
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        val forgot_pass = findViewById<TextView>(R.id.tv_forgot_password)
        forgot_pass.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        val cbShowPassword = findViewById<CheckBox>(R.id.cbShowPassword)
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            togglePasswordVisibility(passwordEditText, isChecked)
        }
    }

    private fun loginUser(mobile: String, password: String) {
        // Check if user exists in the database and password matches
        databaseReference.child(mobile).get().addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                val user = task.result
                if (user != null && user.exists()) {
                    val storedPassword = user.child("password").value.toString()
                    if (storedPassword == password) {
                        // Successful login, save all user details to SharedPreferences
                        val sharedPref = getSharedPreferences("UserLogin", MODE_PRIVATE)
                        val editor = sharedPref.edit()

                        // Save all user details
                        editor.putString("name", user.child("name").value.toString())
                        editor.putString("mobile", mobile)
                        editor.putString("age", user.child("age").value.toString())
                        editor.putString("gender", user.child("gender").value.toString())
                        editor.putString("location", user.child("location").value.toString())

                        // Store isLogged as true
                        editor.putBoolean("isLogged", true)

                        // Apply changes to SharedPreferences
                        editor.apply()

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                        // Proceed to next activity (Dashboard)
                        val intent = Intent(this, Dashboard::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Login Failed. Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePasswordVisibility(passwordField: EditText, showPassword: Boolean) {
        if (showPassword) {
            passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        passwordField.setSelection(passwordField.text?.length ?: 0) // Keeps cursor at the end
    }

    private fun validateLoginFields(
        mobileEditText: EditText,
        passwordEditText: EditText
    ): Boolean {
        var isValid = true

        // Clear previous errors
        mobileEditText.error = null
        passwordEditText.error = null

        val mobile = mobileEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate Mobile Number
        if (mobile.isEmpty()) {
            mobileEditText.error = "Mobile number is required"
            isValid = false
        } else if (mobile.length != 10 || !mobile.matches(Regex("^[0-9]{10}$"))) {
            mobileEditText.error = "Enter a valid 10-digit mobile number"
            isValid = false
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }
}
