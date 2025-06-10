package com.chaitany.agewell;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Exercise_suggestion extends AppCompatActivity {

    private RecyclerView exerciseRecyclerView;
    private ExerciseAdapter exerciseAdapter;
    private LinearProgressIndicator progressBar;
    private TextView progressText;
    private ExtendedFloatingActionButton addExerciseFab;
    private List<Exercise> exerciseList; // Local storage for exercises
    private int completedExercises = 0; // Track completed exercises
    private TextView timerTextView; // TextView to display remaining time
    private TextToSpeech textToSpeech; // TextToSpeech instance
    private boolean isExerciseInProgress = false; // Track if an exercise is currently in progress
    private DatabaseReference exerciseRef; // Firebase reference for exercises

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_suggestion);

        initializeViews();
        setupRecyclerView();
        exerciseList = new ArrayList<>(); // Initialize the exercise list here
        initializeTextToSpeech(); // Initialize TextToSpeech

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = getUserIdFromPreferences(); // Get user ID from SharedPreferences


        if (userId != null) {
            exerciseRef = database.getReference("users").child(userId).child("exercises"); // Reference to user's exercises
            loadExercisesFromFirebase(); // Load exercises from Firebase on app launch
        } else {
            Toast.makeText(this, "User  ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to login activity
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            // finish(); // Close this activity
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        exerciseRecyclerView = findViewById(R.id.exerciseRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        addExerciseFab = findViewById(R.id.addExerciseFab);
        timerTextView = findViewById(R.id.timerTextView); // Initialize the timer TextView

        addExerciseFab.setOnClickListener(v -> showAddExerciseDialog());
    }

    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(this);
        exerciseRecyclerView.setAdapter(exerciseAdapter);
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateProgress() {
        double progress = (completedExercises * 100.0) / exerciseList.size();
        progressBar.setProgress((int) progress);
        progressText.setText(String.format("%.0f%% Complete", progress));
    }

    public void startExercise(Exercise exercise) {
        if (isExerciseInProgress) {
            Toast.makeText(this, "Finish the current exercise before starting a new one.", Toast.LENGTH_SHORT).show();
            return;
        }

        isExerciseInProgress = true; // Set the flag to indicate an exercise is in progress
        int totalTime = exercise.getSets() * exercise.getReps() * exercise.getRestTime() * 1000; // Total time in milliseconds

        timerTextView.setVisibility(View.VISIBLE); // Make sure the timer is visible

        new CountDownTimer(totalTime, 1000) { // Update every second
            int timeLeft = totalTime / 1000; // Convert milliseconds to seconds

            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft--; // Decrement the time left
                timerTextView.setText(String.format("Time Left: %d seconds", timeLeft));

                if (timeLeft % 10 == 0) {
                    announceRemainingTime(timeLeft);
                }
            }

            @Override
            public void onFinish
                    () {
                completedExercises++;
                updateProgress(); // Update progress based on completed exercises
                showExerciseCompletedDialog(exercise);
                timerTextView.setVisibility(View.GONE); // Hide the timer when finished
                isExerciseInProgress = false; // Reset the flag when the exercise is finished
            }
        }.start();
    }

    private void announceRemainingTime(int timeLeft) {
        String message = "Time left: " + timeLeft + " seconds";
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void showExerciseCompletedDialog(Exercise exercise) {
        new AlertDialog.Builder(this)
                .setTitle("Exercise Completed")
                .setMessage("You have completed " + exercise.getName() + "!")
                .setPositiveButton("OK", null)
                .show();
    }

    public void watchExerciseVideo(Exercise exercise) {
        String videoUrl = exercise.getVideoUrl(); // Get the video URL from the exercise object
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(intent);
        } else {
            Toast.makeText(this, "No video available for this exercise", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddExerciseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_exercise);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        TextInputEditText nameInput = dialog.findViewById(R.id.exerciseNameInput);
        TextInputEditText setsInput = dialog.findViewById(R.id.setsInput);
        TextInputEditText repsInput = dialog.findViewById(R.id.repsInput);
        TextInputEditText restTimeInput = dialog.findViewById(R.id.restTimeInput);
        TextInputEditText instructionsInput = dialog.findViewById(R.id.instructionsInput);
        TextInputEditText safetyTipsInput = dialog.findViewById(R.id.safetyTipsInput);
        TextInputEditText videoUrlInput = dialog.findViewById(R.id.videoUrlInput);
        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            try {
                String name = nameInput.getText().toString();
                int sets = Integer.parseInt(setsInput.getText().toString());
                int reps = Integer.parseInt(repsInput.getText().toString());
                int restTime = Integer.parseInt(restTimeInput.getText().toString());
                String instructions = instructionsInput.getText().toString();
                String safetyTips = safetyTipsInput.getText().toString();
                String videoUrl = videoUrlInput.getText().toString();

                Exercise newExercise = new Exercise(name,
                        "Intermediate",
                        sets,
                        reps,
                        restTime,
                        instructions,
                        safetyTips,
                        videoUrl
                );

                // Add exercise to the local list and update RecyclerView
                exerciseList.add(newExercise);
                exerciseAdapter.setExercises(exerciseList);

                // Save exercise data to Firebase under the user's exercises
                String exerciseId = exerciseRef.push().getKey(); // Generate unique ID
                if (exerciseId != null) {
                    exerciseRef.child(exerciseId).setValue(newExercise); // Store exercise data under exerciseId
                }

                dialog.dismiss();
            } catch (NumberFormatException e) {
                showError("Please enter valid numbers");
            }
        });

        dialog.show();
    }

    private void showError(String message) {
        Snackbar.make(exerciseRecyclerView, message, Snackbar.LENGTH_LONG).show();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadExercisesFromFirebase() {
        exerciseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                exerciseList.clear(); // Clear current list

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Exercise exercise = snapshot.getValue(Exercise.class);
                    if (exercise != null) {
                        exerciseList.add(exercise);
                    }
                }

                exerciseAdapter.setExercises(exerciseList);
            } else {
                Toast.makeText(this, "Failed to load exercises", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserIdFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserLogin", MODE_PRIVATE);
        return sharedPreferences .getString("mobile", null); // Use the correct key to retrieve the mobile number
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}