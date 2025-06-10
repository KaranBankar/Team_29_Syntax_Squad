package com.chaitany.agewell

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class ForgotPassword : AppCompatActivity() {

    private lateinit var etMobile: TextInputEditText
    private lateinit var etOtp: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var cbShowPassword: CheckBox
    private lateinit var btnResetPassword: MaterialButton

    private lateinit var database: DatabaseReference
    private var generatedOtp: String = ""
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize Views
        etMobile = findViewById(R.id.etMobile)
        etOtp = findViewById(R.id.etOtp)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        cbShowPassword = findViewById(R.id.cbShowPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnResetPassword.setText("Send Otp")
        etOtp.isEnabled=false

        database = FirebaseDatabase.getInstance().getReference("users")
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)

        // Show/Hide Password
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val inputType = if (isChecked) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            etNewPassword.inputType = inputType
            etConfirmPassword.inputType = inputType
        }

        btnResetPassword.setOnClickListener {
            validateAndProcess()
        }
    }

    private fun validateAndProcess() {
        val mobile = etMobile.text.toString().trim()
        val otp = etOtp.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (mobile.isEmpty() || mobile.length != 10) {
            etMobile.error = "Enter valid 10-digit mobile number"
            return
        }

        if (otp.isNotEmpty() && !otp.matches(Regex("\\d{6}"))) {
            etOtp.error = "Enter valid 6-digit OTP"
            return
        }

        if (otp.isEmpty()) {
            progressDialog.setMessage("Checking user...")
            progressDialog.show()
            checkUserExists(mobile)
        } else {
            if (otp == generatedOtp) {
                if (newPassword.length < 6) {
                    etNewPassword.error = "Password must be at least 6 characters"
                    return
                }
                if (newPassword != confirmPassword) {
                    etConfirmPassword.error = "Passwords do not match"
                    return
                }
                progressDialog.setMessage("Resetting password...")
                progressDialog.show()
                resetPassword(mobile, newPassword)
            } else {
                etOtp.error = "Invalid OTP"
            }
        }
    }

    private fun checkUserExists(mobile: String) {
        database.child(mobile).get().addOnSuccessListener { dataSnapshot ->
            progressDialog.dismiss()
            if (dataSnapshot.exists()) {
                sendOtp(mobile)
            } else {
                Toast.makeText(this, "User not found with this number", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Error checking user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOtp(mobile: String) {
        generatedOtp = (100000..999999).random().toString()
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(mobile, null, "Your OTP is: $generatedOtp", null, null)
            Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show()
            btnResetPassword.setText("Reset Password")
            etOtp.isEnabled=true
            etOtp.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetPassword(mobile: String, newPassword: String) {
        database.child(mobile).child("password").setValue(newPassword).addOnSuccessListener {
            progressDialog.dismiss()
            showSuccessDialog()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Password Reset")
            .setMessage("Your password has been reset successfully!")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }.show()
    }
}
