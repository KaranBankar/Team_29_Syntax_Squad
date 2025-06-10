package com.chaitany.agewell;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityAppointment extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private AppointmentDatabaseHelper dbHelper;
    private TextView thisMonthCount, upcomingCount, pastCount;
    private Chip chipAll, chipUpcoming, chipPast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        // Initialize views
        recyclerView = findViewById(R.id.recycler_appointments);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        thisMonthCount = findViewById(R.id.text_this_month_count);
        upcomingCount = findViewById(R.id.text_upcoming_count);
        pastCount = findViewById(R.id.text_past_count);
        chipAll = findViewById(R.id.chip_all);
        chipUpcoming = findViewById(R.id.chip_upcoming);
        chipPast = findViewById(R.id.chip_past);

        // Initialize database helper
        dbHelper = new AppointmentDatabaseHelper(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Setup FAB click listener
        fabAdd.setOnClickListener(v -> showAddAppointmentDialog());

        // Setup chip group listeners
        setupFilterChips();

        // Load initial data
        loadAppointments("all");
        updateCounts();
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> loadAppointments("all"));
        chipUpcoming.setOnClickListener(v -> loadAppointments("upcoming"));
        chipPast.setOnClickListener(v -> loadAppointments("past"));
    }

    private void showAddAppointmentDialog() {
        AddAppointmentDialog dialog = new AddAppointmentDialog();
        dialog.show(getSupportFragmentManager(), "AddAppointment");
        dialog.setAppointmentAddedListener(appointment -> {
            dbHelper.addAppointment(appointment);
            loadAppointments("all");
            updateCounts();
        });
    }

    private void loadAppointments(String filter) {
        List<Appointment> appointments = dbHelper.getAllAppointments();
        List<Appointment> filteredAppointments = new ArrayList<>();

        for (Appointment appointment : appointments) {
            switch (filter) {
                case "upcoming":
                    if (isUpcoming(appointment)) filteredAppointments.add(appointment);
                    break;
                case "past":
                    if (!isUpcoming(appointment)) filteredAppointments.add(appointment);
                    break;
                default:
                    filteredAppointments.add(appointment);
            }
        }

        adapter.updateAppointments(filteredAppointments);
    }

    private void updateCounts() {
        List<Appointment> appointments = dbHelper.getAllAppointments();
        int thisMonth = 0, upcoming = 0, past = 0;

        for (Appointment appointment : appointments) {
            if (isThisMonth(appointment)) thisMonth++;
            if (isUpcoming(appointment)) upcoming++;
            else past++;
        }

        thisMonthCount.setText(String.valueOf(thisMonth));
        upcomingCount.setText(String.valueOf(upcoming));
        pastCount.setText(String.valueOf(past));
    }

    private boolean isUpcoming(Appointment appointment) {
        // Implementation to check if appointment is upcoming
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date appointmentDate = sdf.parse(appointment.getDate());
            return appointmentDate.after(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isThisMonth(Appointment appointment) {
        // Implementation to check if appointment is in current month
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String appointmentMonth = appointment.getDate().substring(0, 7);
            String currentMonth = sdf.format(new Date());
            return appointmentMonth.equals(currentMonth);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}