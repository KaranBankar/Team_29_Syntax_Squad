package com.chaitany.agewell;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    private StarRatingView starRating; // Custom view for star rating
    private TextInputEditText feedbackInput; // Input field for feedback comment
    private TextInputEditText userNameInput; // Input field for user's name
    private MaterialButton submitButton; // Button to submit feedback
    private ProgressBar progressBar; // Progress bar for loading state

    private DatabaseReference feedbackRef; // Firebase reference for feedback
    private String userPhone; // User's phone number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback); // Set the layout for this activity

        // Get user phone from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("User Login", Context.MODE_PRIVATE);
        userPhone = sharedPreferences.getString("mobile", null);

        // Initialize Firebase to store feedback under the main feedback node
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback"); // Main feedback node

        initializeViews(); // Initialize UI components
        setupListeners(); // Set up button click listeners
    }

    private void initializeViews() {
        // Find views by their IDs
        starRating = findViewById(R.id.star_rating);
        feedbackInput = findViewById(R.id.feedback_input);
        userNameInput = findViewById(R.id.user_name_input); // New field for user's name
        submitButton = findViewById(R.id.button_submit);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        // Set up click listener for the submit button
        submitButton.setOnClickListener(v -> {
            if (validateForm()) { // Validate the form inputs
                submitFeedback(); // Submit the feedback if valid
            }
        });
    }

    private boolean validateForm() {
        // Validate the star rating
        if (starRating.getRating() == 0) {
            showError("Please provide a rating");
            return false;
        }

        // Validate the feedback input
        if (TextUtils.isEmpty(feedbackInput.getText())) {
            feedbackInput.setError("Please provide your feedback");
            return false;
        }

        // Validate the user name input
        if (TextUtils.isEmpty(userNameInput.getText())) {
            userNameInput.setError("Please provide your name");
            return false;
        }

        return true; // Return true if all validations pass
    }

    private void submitFeedback() {
        // Show loading state
        setLoading(true);

        // Create a unique feedback ID
        String feedbackId = feedbackRef.push().getKey();
        if (feedbackId == null) {
            showError("Error creating feedback");
            setLoading(false);
            return;
        }

        // Create feedback object
        Feedback feedback = new Feedback(
                feedbackId,
                userNameInput.getText().toString(), // Get user's name
                starRating.getRating(),
                feedbackInput.getText().toString(),
                userPhone // Store user phone for reference
        );

        // Save feedback to Firebase
        feedbackRef.child(feedbackId).setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    showSuccessDialog(); // Show success dialog on successful submission
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Failed to submit feedback: " + e.getMessage()); // Show error message
                });
    }

    private void setLoading(boolean isLoading) {
        // Enable or disable the submit button and show/hide the progress bar
        submitButton.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        submitButton.setText(isLoading ? "" : "Submit Feedback");
    }

    private void showSuccessDialog() {
        // Show a dialog to inform the user that feedback was submitted successfully
        new MaterialAlertDialogBuilder(this)
                .setTitle("Thank You!")
                .setMessage("Your feedback has been submitted successfully .")
                .setPositiveButton("Continue", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showError(String message) {
        // Show an error message using Snackbar
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}