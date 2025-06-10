package com.chaitany.agewell;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    ImageView useView;
    SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "TaskCompletionPrefs"; // SharedPreferences file name
    private static final String COMPLETED_TASK_PREFIX = "completed_task_";

    private static final String COMPLETION_DATE_PREFIX = "completion_date_";

    Integer stock;

    private LinearLayout layoutEmergencyContact, layoutMedicalStock, layoutHealthMonitor, layoutmealplan,
            layout_bmi_index, layout_elder_connect, layout_exercise,layout_hospital;
    private LinearLayout morningTasksLayout, afternoonTasksLayout, nightTasksLayout;
    private ImageView menuButton,meta;
    private DatabaseReference tasksRef;
    private String userPhone;
    private static final String PREFS_TIME_BLOCKS = "time_blocks";
    private static final String KEY_MORNING_COMPLETED = "morning_done";
    private static final String KEY_AFTERNOON_COMPLETED = "afternoon_done";
    private static final String KEY_NIGHT_COMPLETED = "night_done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        NotificationHelper.createChannel(this);
        checkNotificationPermissions();
        scheduleDailyChecks();

        sharedPreferences = getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        userPhone = sharedPreferences.getString("mobile", null);

        Intent serviceIntent = new Intent(this, VolumeButtonService.class);
        startForegroundService(serviceIntent);



        // Initialize Firebase
        if (userPhone != null) {
            tasksRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userPhone).child("tasks");
        }

        // Handling window insets for a smooth UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.miana), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupNavigationDrawer();
        setupUserProfile();
        setupClickListeners();
        setupTaskSections();
        loadTasks();
        startVolumeButtonService();
    }

    private void startVolumeButtonService() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS
            }, 1);
        } else {
            Intent serviceIntent = new Intent(this, VolumeButtonService.class);
            startForegroundService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Intent serviceIntent = new Intent(this, VolumeButtonService.class);
                startForegroundService(serviceIntent);
            } else {
                Toast.makeText(this, "Permissions required for emergency location sharing", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    private void scheduleDailyChecks() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, TaskReminderReceiver.class);

        // Schedule for 12 PM (noon)
        scheduleAlarm(alarmManager, intent, 12);
        // Schedule for 6 PM
        scheduleAlarm(alarmManager, intent, 18);
        // Schedule for 9 PM
        scheduleAlarm(alarmManager, intent, 21);
    }
    public static void triggerTimeBasedCheck(Context context) {
        // Check morning tasks (after 12 PM)
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 12) {
            checkAndNotifyForTimeBlock(context, KEY_MORNING_COMPLETED, "morning");
        }

        // Check afternoon tasks (after 6 PM)
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 18) {
            checkAndNotifyForTimeBlock(context, KEY_AFTERNOON_COMPLETED, "afternoon");
        }

        // Check night tasks (after 9 PM)
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 21) {
            checkAndNotifyForTimeBlock(context, KEY_NIGHT_COMPLETED, "night");
        }
    }

    private static void checkAndNotifyForTimeBlock(Context context, String prefKey, String timeBlockName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_TIME_BLOCKS, MODE_PRIVATE);
        if (!prefs.getBoolean(prefKey, false)) {
            NotificationHelper.showNotification(
                    context,
                    "You have incomplete " + timeBlockName + " tasks!"
            );
        }
    }

    private void scheduleAlarm(AlarmManager alarmManager, Intent intent, int hour) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                hour, // Unique request code for each alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE here
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }    private void checkTimeBasedTasks() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        SharedPreferences prefs = getSharedPreferences(PREFS_TIME_BLOCKS, MODE_PRIVATE);

        if (hour >= 12 && !prefs.getBoolean(KEY_MORNING_COMPLETED, false)) {
            NotificationHelper.showNotification(this, "Complete your morning tasks!");
        }
        if (hour >= 18 && !prefs.getBoolean(KEY_AFTERNOON_COMPLETED, false)) {
            NotificationHelper.showNotification(this, "Complete your afternoon tasks!");
        }
        if (hour >= 21 && !prefs.getBoolean(KEY_NIGHT_COMPLETED, false)) {
            NotificationHelper.showNotification(this, "Complete your night tasks!");
        }
    }

    private void initializeViews() {
        useView=findViewById(R.id.user_png_dot);
        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.menubars);
        layout_bmi_index = findViewById(R.id.layout_bmi_index);
        layoutEmergencyContact = findViewById(R.id.layout_emergency_contact);
        layoutMedicalStock = findViewById(R.id.layout_medical_stock);
        layoutHealthMonitor = findViewById(R.id.layout_health_monitor);
        layoutmealplan = findViewById(R.id.mealplans);
        layout_elder_connect = findViewById(R.id.layout_elder_connect);
        layout_exercise = findViewById(R.id.layoutexercise);
        layout_hospital=findViewById(R.id.nearbyhospital);
        meta=findViewById(R.id.meta);



        // Initialize task section layouts
        morningTasksLayout = findViewById(R.id.morningTasksLayout);
        afternoonTasksLayout = findViewById(R.id.afternoonTasksLayout);
        nightTasksLayout = findViewById(R.id.nightTasksLayout);
    }

    private void setupNavigationDrawer() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupUserProfile() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        SharedPreferences sharedPref = getSharedPreferences("UserLogin", MODE_PRIVATE);
        String name = sharedPref.getString("name", "User Name");
        String mobile = sharedPref.getString("mobile", "XXXX XX XXXX");
        String gender = sharedPref.getString("gender", "Gender");

        TextView username = findViewById(R.id.username);
        ImageView userimg = findViewById(R.id.user_png_dot);
        TextView usernameTextView = headerView.findViewById(R.id.username);
        TextView mobileTextView = headerView.findViewById(R.id.mobile);
        ImageView user_png = headerView.findViewById(R.id.user_png);

        if (gender.equals("Male")) {
            user_png.setImageResource(R.drawable.img_oldman);
            userimg.setImageResource(R.drawable.img_oldman);
        }
        if (gender.equals("Female")) {
            user_png.setImageResource(R.drawable.img_oldfemale);
            userimg.setImageResource(R.drawable.img_oldfemale);
        }

        username.setText("Hi, " + name);
        usernameTextView.setText(name);
        mobileTextView.setText(mobile);
    }



    private void setupTaskSections() {
        if (morningTasksLayout != null) {
            setupTaskSection(morningTasksLayout, "Morning");
        }
        if (afternoonTasksLayout != null) {
            setupTaskSection(afternoonTasksLayout, "Afternoon");
        }
        if (nightTasksLayout != null) {
            setupTaskSection(nightTasksLayout, "Night");
        }
    }

    private void setupTaskSection(LinearLayout layout, final String timeOfDay) {
        // Find the TextView within the layout (second child)
        TextView titleText = (TextView) ((LinearLayout) layout).getChildAt(1);

        // Create content container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(32, 8, 32, 8);
        contentLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        contentLayout.setVisibility(View.GONE);

        // Add content layout after the header
        ViewGroup parent = (ViewGroup) layout.getParent();
        parent.addView(contentLayout, parent.indexOfChild(layout) + 1);

        // Set up click listener
        layout.setOnClickListener(v -> {
            boolean isExpanded = contentLayout.getVisibility() == View.VISIBLE;
            // Toggle visibility with animation
            if (isExpanded) {
                contentLayout.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> contentLayout.setVisibility(View.GONE))
                        .start();
            } else {
                contentLayout.setAlpha(0f);
                contentLayout.setVisibility(View.VISIBLE);
                contentLayout.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            }

            // Rotate arrow
            titleText.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.down_icon,
                    0
            );

            // Animate arrow rotation
            Drawable[] drawables = titleText.getCompoundDrawables();
            if (drawables[2] != null) {
                drawables[2].setLevel(isExpanded ? 0 : 10000);
                ObjectAnimator.ofInt(drawables[2], "level", isExpanded ? 10000 : 0)
                        .setDuration(200)
                        .start();
            }
        });
    }

    private void loadTasks() {
        if (tasksRef == null) return;

        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clearAllTasks();
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);

                    if (task != null) {

                        // Ensure task name is not null before comparison
                        if (task.getTaskName() != null && "Take medicine".equals(task.getTaskName())) {
                            String medicineId = task.getMedicineId(); // Ensure correct field name

                            if (medicineId != null) {
                                //fetchMedicineStock(medicineId);

                            } else {
                                Log.e("TaskError", "Medicine ID is null for task: " + task.getTaskName());
                            }
                        }

                        Log.d("FirebaseTask", "Task Fetched: " +
                                "\nTask Name: " + task.getTaskName() +
                                "\nTime: " + task.getTime() +
                                "\nMeal Time: " + task.getMealTime() +
                                "\nQuantity: " + task.getQuantity() +
                                "\nMedicine ID: " + task.getMedicineId() +
                                "\nMedicine Name: " + task.getMedicineName());
                        addTaskToSection(task);




                    }
                }
                addPredefinedTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, "Failed to load tasks: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }







    private void clearAllTasks() {
        clearTaskSection(morningTasksLayout);
        clearTaskSection(afternoonTasksLayout);
        clearTaskSection(nightTasksLayout);
    }

    private void clearTaskSection(LinearLayout sectionLayout) {
        ViewGroup parent = (ViewGroup) sectionLayout.getParent();
        int index = parent.indexOfChild(sectionLayout);
        if (index + 1 < parent.getChildCount()) {
            View contentView = parent.getChildAt(index + 1);
            if (contentView instanceof LinearLayout) {
                ((LinearLayout) contentView).removeAllViews();
            }
        }
    }

    private void addTaskToSection(Task task) {
        LinearLayout targetSection2;
        String sectionTime = task.getTime().toLowerCase();
        ViewGroup parent;

        // Determine the target section based on the task time
        if (sectionTime.contains("morning")) {
            parent = (ViewGroup) morningTasksLayout.getParent();
            targetSection2 = (LinearLayout) parent.getChildAt(parent.indexOfChild(morningTasksLayout) + 1);
        } else if (sectionTime.contains("afternoon")) {
            parent = (ViewGroup) afternoonTasksLayout.getParent();
            targetSection2 = (LinearLayout) parent.getChildAt(parent.indexOfChild(afternoonTasksLayout) + 1);
        } else if (sectionTime.contains("night")) {
            parent = (ViewGroup) nightTasksLayout.getParent();
            targetSection2 = (LinearLayout) parent.getChildAt(parent.indexOfChild(nightTasksLayout) + 1);
        } else {
            targetSection2 = null;
        }

        if (targetSection2 != null) {
            // Check if the task has already been completed today
            if (isTaskCompleted(task)) {
                return; // Skip adding the task if already completed
            }

            final View taskView = getLayoutInflater().inflate(R.layout.item_task, targetSection2, false);

            // Add margin between items
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            taskView.setLayoutParams(params);
            TextView stockValueText = taskView.findViewById(R.id.tvStockValue);

            DatabaseReference medicineRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userPhone).child("medicines");

            final int[] stock = {0};
            if (task.getTaskName().equals("Take medicine")) {
                TextView finalStockValueText1 = stockValueText;
                medicineRef.orderByChild("id").equalTo(task.getMedicineId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot medicineSnapshot : dataSnapshot.getChildren()) {
                                        String name = medicineSnapshot.child("name").getValue(String.class);
                                        Integer quantity = medicineSnapshot.child("quantity").getValue(Integer.class);
                                        stock[0] = quantity;
                                        finalStockValueText1.setText("" + quantity + " tablets");
                                        Log.d("Hello", "onDataChange: " + quantity + " tablets ");
                                    }
                                } else {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("MedicineStock", "Failed to fetch stock: " + error.getMessage());
                            }
                        });
            }

            TextView medicineNameText = taskView.findViewById(R.id.tvMedicineName);
            TextView mealTimeText = taskView.findViewById(R.id.tvMealTime);

            CheckBox completeTaskCheckBox = taskView.findViewById(R.id.cbCompleteTask);

            medicineNameText.setText(task.getTaskName() + " :" + task.getMedicineName());
            mealTimeText.setText(task.getMealTime());

            // Set up checkbox listener with Firebase integration
            TextView finalStockValueText = stockValueText;
            completeTaskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    new AlertDialog.Builder(Dashboard.this)
                            .setTitle("Task Completion")
                            .setMessage("Are you sure you want to mark this task as completed?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Check if the task name is "Take medicine"
                                if ("Take medicine".equals(task.getTaskName())) {

                                    // Fetch the medicine ID from the task object
                                    String medicineId = task.getMedicineId();
                                    if (medicineId != null) {
                                        // Fetch the current stock quantity from Firebase
                                        DatabaseReference medicineRef1 = FirebaseDatabase.getInstance().getReference("users")
                                                .child(userPhone).child("medicines").child(medicineId).child("quantity");

                                        medicineRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    // Get current stock quantity
                                                    int currentStock = dataSnapshot.getValue(Integer.class);

                                                    // Ensure stock is available
                                                    if (currentStock > 0) {
                                                        // Decrease stock by 1
                                                        int newQuantity = currentStock - 1;

                                                        // Update quantity in Firebase
                                                        medicineRef1.setValue(newQuantity)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    finalStockValueText.setText(newQuantity + " tablets");
                                                                    task.setQuantity(newQuantity);  // Update local task quantity

                                                                    // Remove task view if stock is 0
                                                                    targetSection2.removeView(taskView);
                                                                    markTaskAsCompleted(task);

                                                                    // Show success message
                                                                    Toast.makeText(Dashboard.this,
                                                                            "Task completed! Remaining tablets: " + newQuantity,
                                                                            Toast.LENGTH_SHORT).show();
                                                                    completeTaskCheckBox.setChecked(false);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    // Handle failure to update quantity
                                                                    Toast.makeText(Dashboard.this,
                                                                            "Failed to update quantity: " + e.getMessage(),
                                                                            Toast.LENGTH_SHORT).show();
                                                                    completeTaskCheckBox.setChecked(false);
                                                                });
                                                    } else {
                                                        // Handle case where stock is zero
                                                        Toast.makeText(Dashboard.this, "No tablets left!", Toast.LENGTH_SHORT).show();
                                                        completeTaskCheckBox.setChecked(false);
                                                    }
                                                } else {
                                                    // Handle case where medicine is not found
                                                    Toast.makeText(Dashboard.this, "Medicine not found!", Toast.LENGTH_SHORT).show();
                                                    completeTaskCheckBox.setChecked(false);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // Handle Firebase read error
                                                Toast.makeText(Dashboard.this,
                                                        "Failed to fetch stock: " + error.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                                completeTaskCheckBox.setChecked(false);
                                            }
                                        });
                                    } else {
                                        // Handle case where medicine ID is null
                                        Toast.makeText(Dashboard.this, "Medicine ID is missing!", Toast.LENGTH_SHORT).show();
                                        completeTaskCheckBox.setChecked(false);
                                    }
                                } else {
                                    // Handle case where task name is not "Take medicine"
                                    Toast.makeText(Dashboard.this, "This task is not for medicine!", Toast.LENGTH_SHORT).show();
                                    completeTaskCheckBox.setChecked(false);
                                }
                            })
                            .setNegativeButton("No", (dialog, which) -> completeTaskCheckBox.setChecked(false))
                            .show();
                }
            });

            targetSection2.addView(taskView);

        }
    }

    // New method to add predefined tasks based on elderly activities
    private void addPredefinedTasks() {
        // Predefined tasks for morning, afternoon, and night
        addPredefinedTaskToSection(new Task("Jogging", "morning", "Morning", 1, "1","fff"));
        addPredefinedTaskToSection(new Task("Yoga", "morning", "Morning", 1, "2","fff"));
        addPredefinedTaskToSection(new Task("Exercise", "morning", "Morning", 1,"3","fff"));
        addPredefinedTaskToSection(new Task("Nap", "afternoon", "Afternoon", 1,"4","fff"));
        addPredefinedTaskToSection(new Task("Walk", "afternoon", "Afternoon", 1,"5","fff"));
        addPredefinedTaskToSection(new Task("Lunch", "afternoon", "Afternoon", 1,"6","fff"));
        addPredefinedTaskToSection(new Task("Dinner", "night", "Night", 1,"7","fff"));
        addPredefinedTaskToSection(new Task("Evening Walk", "night", "Night", 1,"8","fff"));
        addPredefinedTaskToSection(new Task("Reading", "night", "Night", 1,"9","fff"));
    }

    private void addPredefinedTaskToSection(Task task) {
        final LinearLayout targetSection; // Declare as final
        String sectionTime = task.getTime().toLowerCase();
        ViewGroup parent;

        // Determine the target section based on the task time
        if (sectionTime.contains("morning")) {
            parent = (ViewGroup) morningTasksLayout.getParent();
            targetSection = (LinearLayout) parent.getChildAt(parent.indexOfChild(morningTasksLayout) + 1);
        } else if (sectionTime.contains("afternoon")) {
            parent = (ViewGroup) afternoonTasksLayout.getParent();
            targetSection = (LinearLayout) parent.getChildAt(parent.indexOfChild(afternoonTasksLayout) + 1);
        } else if (sectionTime.contains("night")) {
            parent = (ViewGroup) nightTasksLayout.getParent();
            targetSection = (LinearLayout) parent.getChildAt(parent.indexOfChild(nightTasksLayout) + 1);
        } else {
            return; // If no valid section is found, exit the method
        }

        if (targetSection != null) {
            // Check if the task has already been completed today
            if (isTaskCompleted(task)) {
                return; // Skip adding the task if already completed
            }

            View taskView = getLayoutInflater().inflate(R.layout.item_predefined_task, targetSection, false);

            // Add margin between items
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 4, 0, 4);
            taskView.setLayoutParams(params);

            TextView taskNameText = taskView.findViewById(R.id.tvPredefinedTaskName);
            TextView taskTimeText = taskView.findViewById(R.id.tvPredefinedTaskTime);
            CheckBox completeTaskCheckBox = taskView.findViewById(R.id.cbCompleteTask);

            taskNameText.setText(task.getTaskName());
            taskTimeText.setText(task.getMealTime());

            // Set up checkbox listener
            completeTaskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Show confirmation dialog
                    new AlertDialog.Builder(Dashboard.this)
                            .setTitle("Task Completion")
                            .setMessage("Are you sure you want to mark this task as completed?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Remove the task from the target section
                                targetSection.removeView(taskView);
                                // Mark the task as completed
                                markTaskAsCompleted(task);
                                // Show a toast message
                                Toast.makeText(Dashboard.this, "Task completed!", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                // Uncheck the checkbox if the user cancels
                                completeTaskCheckBox.setChecked(false);
                            })
                            .show();
                }
            });

            targetSection.addView(taskView);
        }
    }

    private void markTaskAsCompleted(Task task) {
        SharedPreferences timeBlockPrefs = getSharedPreferences(PREFS_TIME_BLOCKS, MODE_PRIVATE);
        String timeBlock = getTimeBlockForTask(task);

        // Mark individual task completion
        SharedPreferences taskPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        taskPrefs.edit()
                .putBoolean(COMPLETED_TASK_PREFIX + task.getMedicineId(), true)
                .putString(COMPLETION_DATE_PREFIX + task.getMedicineId(), getCurrentDate())
                .apply();

        // Check if all tasks in this time block are completed
        if (allTasksInTimeBlockCompleted(timeBlock)) {
            timeBlockPrefs.edit().putBoolean(timeBlock, true).apply();
        }
    }

    private String getTimeBlockForTask(Task task) {
        String time = task.getTime().toLowerCase();
        if (time.contains("morning")) return KEY_MORNING_COMPLETED;
        if (time.contains("afternoon")) return KEY_AFTERNOON_COMPLETED;
        return KEY_NIGHT_COMPLETED;
    }

    private boolean allTasksInTimeBlockCompleted(String timeBlockKey) {
        // Implement logic to check if all tasks in the time block are completed
        // This might require querying your Firebase tasks
        return false; // Temporary return value
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private boolean isTaskCompleted(Task task) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String taskKey = COMPLETED_TASK_PREFIX + task.getMedicineId();
        String completionDateKey = COMPLETION_DATE_PREFIX + task.getMedicineId();

        String storedDate = preferences.getString(completionDateKey, null);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // If the task was completed on a different date, mark it as incomplete again
        if (storedDate != null && !storedDate.equals(currentDate)) {
            // The task was completed on a previous day, so we reset it to incomplete for today
            preferences.edit().putBoolean(taskKey, false).apply();
            return false;
        }

        return preferences.getBoolean(taskKey, false); // Return whether the task was completed
    }

    private void handleNewDay() {
        // Reset time block completions
        SharedPreferences timeBlockPrefs = getSharedPreferences(PREFS_TIME_BLOCKS, MODE_PRIVATE);
        timeBlockPrefs.edit()
                .remove(KEY_MORNING_COMPLETED)
                .remove(KEY_AFTERNOON_COMPLETED)
                .remove(KEY_NIGHT_COMPLETED)
                .apply();

        // Reset individual task completions
        SharedPreferences taskPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = taskPrefs.edit();
        for (Map.Entry<String, ?> entry : taskPrefs.getAll().entrySet()) {
            if (entry.getKey().startsWith(COMPLETED_TASK_PREFIX)) {
                editor.remove(entry.getKey());
            }
        }
        editor.apply();
    }


    private void resetTasksForNewDay() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Reset all completed task flags for the new day (for example, at midnight)
        editor.clear(); // Alternatively, you can reset specific task flags if needed
        editor.apply();
    }

    private void setupClickListeners(){

        useView.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, ProfileActivity.class)));
        layout_bmi_index.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, BMIActivity.class)));
        layoutEmergencyContact.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, EmergencyContact.class)));
        layout_elder_connect.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, globalchat.class)));
        layoutMedicalStock.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, MedicalStockActivity.class)));
        layoutHealthMonitor.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, ActivityAppointment.class)));
        layoutmealplan.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, HealthReportActivity.class)));
        layout_exercise.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, Exercise_suggestion.class)));
        layout_hospital.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, HospitalActivity.class)));

        meta.setOnClickListener(v->{



            startActivity(new Intent(Dashboard.this, AiActivity.class));
        });
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLogged", false);
            editor.apply();
            intent = new Intent(Dashboard.this, Login.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_profile) {
            intent = new Intent(Dashboard.this, ProfileActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_share) {
            shareApp();
        } else if (item.getItemId() == R.id.nav_feedback) {
            intent = new Intent(Dashboard.this, FeedbackActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_about) {
//            intent = new Intent(Dashboard.this, AboutActivity.class);
//            startActivity(intent);
        } else {
            return false;
        }






        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        String appLink = "https://play.google.com/store/apps/details?id=com.chaitany.agewell";
        String shareMessage = "Check out this amazing app that helps you and your older person to stay healthy and care! \n\n" +
                "Download it here: " + appLink + "\n\n" +
                "Stay healthy and fit!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Our App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    // Task model class
    public static class Task {
        private String medicineName;
        private String medicineId;
        private String time;

        private String mealTime;
        private int quantity;
        private String taskName;

        public Task() {
            this.medicineName = "Unknown Medicine";  // Set default value
        }

        // Required empty constructor for Firebase
        public Task(String medicineName) {
            this.medicineName = medicineName;
        }

        // Constructor for predefined tasks
        public Task(String taskname, String time, String mealTime, int quantity, String medicineId,String medicineName) {
            this.taskName = taskname;
            this.time = time;
            this.mealTime = mealTime;
            this.quantity = quantity;
            this.medicineId = medicineId;
            this.medicineName = medicineName;
        }

        // Getters and setter
        //
        // s

        public String getMedicineName() {
            return medicineName;
        }
        public void setMedicineName() {
            this.medicineName = medicineName;
        }

        public String getMedicineId() {
            return medicineId;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMealTime() {
            return mealTime;
        }

        public void setMealTime(String mealTime) {
            this.mealTime = mealTime;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }


    }
    @Override
    protected void onResume() {
        super.onResume();

        // Call the handleNewDay function to check and update tasks for the new day
        handleNewDay();
    }

}