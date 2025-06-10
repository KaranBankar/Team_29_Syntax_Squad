package com.chaitany.agewell;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge, etMobile, etAddress, etCity, etState, etPincode, etHeight, etWeight;
    private RadioGroup radioGender;
    private Button btnSaveProfile;

    private DatabaseReference userRef;
    private String mobileNumber; // Mobile number from SharedPreferences
    private Map<String, Object> updatedFields = new HashMap<>(); // To store modified fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");

        // Get mobile number from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserLogin", MODE_PRIVATE);
        mobileNumber = preferences.getString("mobile", "9322067937");

        if (TextUtils.isEmpty(mobileNumber)) {
            Toast.makeText(this, "Mobile number not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etPincode = findViewById(R.id.etPincode);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        radioGender = findViewById(R.id.radioGender);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Fetch user data
        fetchUserData();

        // Save profile updates
        btnSaveProfile.setOnClickListener(v -> validateAndUpdateProfile());
    }

    private void fetchUserData() {
        // Show progress dialog while fetching user data
        ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Fetching user data...");
        progressDialog.setCancelable(false); // Prevent user from dismissing the dialog
        progressDialog.show();

        userRef.child(mobileNumber).get().addOnCompleteListener(task -> {
            progressDialog.dismiss(); // Dismiss the progress dialog after task completes

            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();

                etName.setText(snapshot.child("name").getValue(String.class));
                etAge.setText(snapshot.child("age").getValue(String.class));
                etAddress.setText(snapshot.child("location").getValue(String.class));
                etCity.setText(snapshot.child("city").getValue(String.class));
                etState.setText(snapshot.child("state").getValue(String.class));
                etPincode.setText(snapshot.child("pincode").getValue(String.class));
                etHeight.setText(snapshot.child("height").getValue(String.class));
                etWeight.setText(snapshot.child("weight").getValue(String.class));

                String gender = snapshot.child("gender").getValue(String.class);
                if ("Male".equalsIgnoreCase(gender)) {
                    radioGender.check(R.id.rbMale);
                } else if ("Female".equalsIgnoreCase(gender)) {
                    radioGender.check(R.id.rbFemale);
                }

                enableEditing();
            } else {
                Toast.makeText(ProfileActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void enableEditing() {
        etName.setEnabled(true);
        etAge.setEnabled(true);
        etAddress.setEnabled(true);
        etCity.setEnabled(true);
        etState.setEnabled(true);
        etPincode.setEnabled(true);
        etHeight.setEnabled(true);
        etWeight.setEnabled(true);
        radioGender.setEnabled(true);

        btnSaveProfile.setVisibility(View.VISIBLE);
    }

    private void validateAndUpdateProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();

        // Validation checks
        if (name.isEmpty() || age.isEmpty() || address.isEmpty() || city.isEmpty() ||
                state.isEmpty() || pincode.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pincode.matches("\\d{6}")) {
            Toast.makeText(this, "Pincode must be 6 digits!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!age.matches("\\d+") || Integer.parseInt(age) < 1 || Integer.parseInt(age) > 120) {
            Toast.makeText(this, "Enter a valid age!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!height.matches("\\d+") || Integer.parseInt(height) < 50 || Integer.parseInt(height) > 250) {
            Toast.makeText(this, "Enter a valid height (50-250 cm)!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!weight.matches("\\d+") || Integer.parseInt(weight) < 20 || Integer.parseInt(weight) > 200) {
            Toast.makeText(this, "Enter a valid weight (20-200 kg)!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog while updating profile
        ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false); // Prevents the user from dismissing the dialog
        progressDialog.show();

        // Fetch current data from Firebase and compare with the new input
        userRef.child(mobileNumber).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        // Get current data from Firebase
                        String currentName = dataSnapshot.child("name").getValue(String.class);
                        String currentAge = dataSnapshot.child("age").getValue(String.class);
                        String currentAddress = dataSnapshot.child("address").getValue(String.class);
                        String currentCity = dataSnapshot.child("city").getValue(String.class);
                        String currentState = dataSnapshot.child("state").getValue(String.class);
                        String currentPincode = dataSnapshot.child("pincode").getValue(String.class);
                        String currentHeight = dataSnapshot.child("height").getValue(String.class);
                        String currentWeight = dataSnapshot.child("weight").getValue(String.class);

                        // Compare with new values and add to updatedFields if changed
                        if (!name.equals(currentName)) updatedFields.put("name", name);
                        if (!age.equals(currentAge)) updatedFields.put("age", age);
                        if (!address.equals(currentAddress)) updatedFields.put("address", address);
                        if (!city.equals(currentCity)) updatedFields.put("city", city);
                        if (!state.equals(currentState)) updatedFields.put("state", state);
                        if (!pincode.equals(currentPincode)) updatedFields.put("pincode", pincode);
                        if (!height.equals(currentHeight)) updatedFields.put("height", height);
                        if (!weight.equals(currentWeight)) updatedFields.put("weight", weight);

                        // Now update only changed fields
                        if (!updatedFields.isEmpty()) {
                            userRef.child(mobileNumber).updateChildren(updatedFields)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss(); // Dismiss progress dialog
                                        // Show success dialog
                                        new AlertDialog.Builder(ProfileActivity.this)
                                                .setTitle("Profile Updated")
                                                .setMessage("Your profile has been updated successfully!")
                                                .setPositiveButton("OK", (dialog, which) -> {
                                                    // Navigate to Dashboard Activity
                                                    Intent intent = new Intent(ProfileActivity.this, Dashboard.class);
                                                    startActivity(intent);
                                                    finish(); // Optionally finish this activity
                                                })
                                                .setCancelable(false)
                                                .show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss(); // Dismiss progress dialog
                                        Toast.makeText(ProfileActivity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressDialog.dismiss(); // Dismiss progress dialog
                            Toast.makeText(ProfileActivity.this, "No changes detected!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // Dismiss progress dialog
                    Toast.makeText(ProfileActivity.this, "Failed to fetch current profile data!", Toast.LENGTH_SHORT).show();
                });
    }







}
