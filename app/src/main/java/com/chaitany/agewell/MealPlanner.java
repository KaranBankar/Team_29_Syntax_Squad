package com.chaitany.agewell;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MealPlanner extends AppCompatActivity {
    private LinearLayout mealContainer;
    private String userId;
    private SharedPreferences sharedPreferences;
    private DatabaseReference db;

    // Declare EditText variables for dialog
    private TextInputEditText etMeal, etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);

        sharedPreferences = getSharedPreferences("UserLogin", MODE_PRIVATE);
        db = FirebaseDatabase.getInstance().getReference("users");

        mealContainer = findViewById(R.id.mealContainer);
        Button btnAddMeal = findViewById(R.id.btnAddMeal);


        userId = sharedPreferences.getString("mobile", null);
        if (userId == null) {
            Toast.makeText(MealPlanner.this, "User  ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddMeal.setOnClickListener(v -> openAddEditMealDialog(null));


        loadMeals(); // Load meals when the activity starts
    }

    private void loadMeals() {
        mealContainer.removeAllViews();  // Clear existing views before loading new data
        db.child(userId).child("meals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {  // Check if data exists
                    for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                        Meal meal = mealSnapshot.getValue(Meal.class);
                        if (meal != null) {
                            meal.setMealId(mealSnapshot.getKey());  // Set the meal ID
                            addMealToContainer(meal);
                        }
                    }
                } else {
                    Toast.makeText(MealPlanner.this, "No meals found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MealPlanner.this, "Error loading meals: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addMealToContainer(Meal meal) {
        if (meal != null) {
            View mealView = getLayoutInflater().inflate(R.layout.mealmenu_item, mealContainer, false);
            Spinner spinnerDay = mealView.findViewById(R.id.spinnerDay);
            TextView tvMeal = mealView.findViewById(R.id.tvMeal);
            TextView tvNotes = mealView.findViewById(R.id.tvNotes);
            Button btnEdit = mealView.findViewById(R.id.btnEdit);
            Button btndelete = mealView.findViewById(R.id.btnDelete);

            // Set the selected day
            if (meal.getDay() != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this, R.array.days_of_week, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDay.setAdapter(adapter);
                int position = adapter.getPosition(meal.getDay());
                spinnerDay.setSelection(position);
            }

            // Check if meal properties are null
            if (meal.getMeal() == null || meal.getNotes() == null) {
                Log.e("MealPlanner", "Meal properties are null");
                return; // Exit if any property is null
            }

            tvMeal.setText(meal.getMeal());
            tvNotes.setText("Notes: " + meal.getNotes());

            mealView.setTag(meal.getMeal());
            btnEdit.setOnClickListener(v -> openAddEditMealDialog(mealView));

            // Delete button functionality
            btndelete.setOnClickListener(v -> {
                String mealId = meal.getMealId();  // Use mealId instead of meal name
                db.child(userId).child("meals").child(mealId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MealPlanner.this, "Meal deleted successfully", Toast.LENGTH_SHORT).show();
                            mealContainer.removeView(mealView);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MealPlanner.this, "Error deleting meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });


            mealContainer.addView(mealView);

        } else {
            Log.e("MealPlanner", "Meal is null");
        }
    }

    private void openAddEditMealDialog(View mealItem) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_edit_meal);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Initialize views
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        Spinner spinnerDay = dialog.findViewById(R.id.spinnerDay);
        TextInputEditText etMeal = dialog.findViewById(R.id.etMeal);
        TextInputEditText etNotes = dialog.findViewById(R.id.etNotes);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Set up Spinner for new meal
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);

        if (mealItem != null) {
            // Editing an existing meal
            tvDialogTitle.setText("Edit Meal");
            TextView tvMeal = mealItem.findViewById(R.id.tvMeal);
            TextView tvNotes = mealItem.findViewById(R.id.tvNotes);
            Spinner spinnerMealDay = mealItem.findViewById(R.id.spinnerDay);

            etMeal.setText(tvMeal.getText().toString());
            etNotes.setText(tvNotes.getText().toString().replace("Notes: ", ""));

            // Get the selected day from the existing meal
            String selectedDay = spinnerMealDay.getSelectedItem().toString();
            int position = adapter.getPosition(selectedDay);
            spinnerDay.setSelection(position);
        } else {
            // Adding a new meal
            tvDialogTitle.setText("Add New Meal");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> saveMealData(dialog, spinnerDay, etMeal, etNotes, mealItem));

        dialog.show();
    }

    private void saveMealData(Dialog dialog, Spinner day, TextInputEditText etMeal, TextInputEditText etNotes, View mealItem) {
        String meal = etMeal.getText().toString().trim();
        String dayy = day.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();

        if (meal.isEmpty()) {
            Toast.makeText(MealPlanner.this, "Meal cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Meal mealData = new Meal(dayy, meal, notes);

        if (mealItem != null) {
            // **Updating an existing meal**
            String mealId = mealItem.getTag().toString();
            db.child(userId).child("meals").child(mealId).setValue(mealData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MealPlanner.this, "Meal updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadMeals();  // **Refresh the meal list after update**
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MealPlanner.this, "Error updating meal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // **Adding a new meal**
            String mealId = db.child(userId).child("meals").push().getKey();
            db.child(userId).child("meals").child(mealId).setValue(mealData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MealPlanner.this, "Meal saved successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadMeals();  // **Refresh the meal list after adding new meal**
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MealPlanner.this, "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // Meal data model class
    public static class Meal {
        private String mealId;  // Add this field
        private String day;
        private String meal;
        private String notes;

        public Meal() { }

        public Meal(String day, String meal, String notes) {
            this.day = day;
            this.meal = meal;
            this.notes = notes;
        }

        // Getters and Setters
        public String getMealId() {
            return mealId;
        }

        public void setMealId(String mealId) {
            this.mealId = mealId;
        }

        public String getMeal() {
            return meal;
        }

        public String getNotes() {
            return notes;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }
    }

}