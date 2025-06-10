package com.chaitany.agewell;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<Exercise> exercises;
    private Context context;
    private TextToSpeech textToSpeech;

    public ExerciseAdapter(Context context) {
        this.context = context;
        this.exercises = new ArrayList<>();
        initializeTextToSpeech();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView exerciseName;
        private Chip difficultyChip;
        private TextView exerciseDetails;
        private TextView restTime;
        private MaterialButton expandButton;
        private LinearLayout expandedContent;
        private TextView instructions;
        private TextView safetyTips;
        private MaterialButton startButton;
        private MaterialButton watchVideoButton;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            difficultyChip = itemView.findViewById(R.id.difficultyChip);
            exerciseDetails = itemView.findViewById(R.id.exerciseDetails);
            restTime = itemView.findViewById(R.id.restTime);
            expandButton = itemView.findViewById(R.id.expandButton);
            expandedContent = itemView.findViewById(R.id.expandedContent);
            instructions = itemView.findViewById(R.id.instructions);
            safetyTips = itemView.findViewById(R.id.safetyTips);
            startButton = itemView.findViewById(R.id.startButton);
            watchVideoButton = itemView.findViewById(R.id.watchVideoButton);

            setupListeners();
        }

        private void setupListeners() {
            expandButton.setOnClickListener(v -> {
                boolean isExpanded = expandedContent.getVisibility() == View.VISIBLE;
                expandedContent.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                expandButton.setText(isExpanded ? "Show Instructions" : "Hide Instructions");
                expandButton.setIconResource(isExpanded ? R.drawable.down_icon : R.drawable.ic_add);
            });

            startButton.setOnClickListener(v -> {
                Exercise exercise = exercises.get(getAdapterPosition());
                startExercise(exercise);
            });

            watchVideoButton.setOnClickListener(v -> {
                Exercise exercise = exercises.get(getAdapterPosition());
                watchExerciseVideo(exercise);
            });
        }

        private void startExercise(Exercise exercise) {
            // Validate exercise data
            if (exercise.getName() == null || exercise.getSets() <= 0 || exercise.getReps() <= 0) {
                Toast.makeText(context, "Invalid exercise data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start exercise timer or any logic to track exercise progress
            ((Exercise_suggestion) context).startExercise(exercise);
            announceRemainingTime(exercise);  // Optionally announce start using TextToSpeech
        }

        public void bind(Exercise exercise) {
            exerciseName.setText(exercise.getName());
            difficultyChip.setText(exercise.getLevel());
            exerciseDetails.setText(String.format("%d sets of %d repetitions", exercise.getSets(), exercise.getReps()));
            restTime.setText(String.format("Rest: %d seconds between sets", exercise.getRestTime()));
            instructions.setText(exercise.getInstructions());
            safetyTips.setText(exercise.getSafetyTips());
            expandedContent.setVisibility(View.GONE);  // Collapse expanded content initially
        }

        private void watchExerciseVideo(Exercise exercise) {
            String videoUrl = exercise.getVideoUrl();
            if (videoUrl == null || videoUrl.isEmpty()) {
                Toast.makeText(context, "No video available for this exercise", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            context.startActivity(intent);
            Toast.makeText(context, "Opening video for " + exercise.getName(), Toast.LENGTH_SHORT).show();
        }

        private void announceRemainingTime(Exercise exercise) {
            String message = "Starting exercise: " + exercise.getName();
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // Release TextToSpeech resources when no longer needed
    public void releaseResources() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}