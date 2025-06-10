package com.chaitany.agewell;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class AddAppointmentDialog extends DialogFragment {
    private AppointmentAddedListener listener;
    private EditText doctorInput, typeInput;
    private TextView dateText, timeText;

    private String selectedDate = "", selectedTime = "",  selectedReminder = "";

    public interface AppointmentAddedListener {
        void onAppointmentAdded(Appointment appointment);
    }

    public void setAppointmentAddedListener(AppointmentAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_appointment, null);

        // Initialize views
        doctorInput = view.findViewById(R.id.input_doctor);
        typeInput = view.findViewById(R.id.input_type);
        dateText = view.findViewById(R.id.text_date);
        timeText = view.findViewById(R.id.text_time);


        // Set up recurrence and reminder spinners



        // Set up date and time pickers
        dateText.setOnClickListener(v -> showDatePicker());
        timeText.setOnClickListener(v -> showTimePicker());

        builder.setView(view)
                .setTitle("Add New Appointment")
                .setPositiveButton("Add", (dialog, id) -> {
                    if (validateInputs()) {
                        Appointment appointment = new Appointment(
                                0, // ID (0 for new appointments)
                                selectedDate, // Selected date
                                selectedTime, // Selected time
                                doctorInput.getText().toString(), // Doctor's name
                                typeInput.getText().toString() // Appointment type

                        );
                        listener.onAppointmentAdded(appointment); // Notify listener
                        addEventToCalendar(appointment); // Add event to calendar
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss()); // Dismiss dialog on cancel

        return builder.create();
    }

    private void addEventToCalendar(Appointment appointment) {
        try {
            Calendar calendar = Calendar.getInstance();
            String[] dateParts = appointment.getDate().split("-");
            String[] timeParts = appointment.getTime().split(":");

            calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));

            long startMillis = calendar.getTimeInMillis();
            long endMillis = startMillis + (60 * 60 * 1000); // Appointment duration: 1 hour

            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "Appointment with Dr. " + appointment.getDoctorName());
            values.put(CalendarContract.Events.DESCRIPTION, "Type: " + appointment.getType());
            values.put(CalendarContract.Events.CALENDAR_ID, 1); // Make sure to use the correct calendar ID
            values.put(CalendarContract.Events.EVENT_TIMEZONE, calendar.getTimeZone().getID());

            requireActivity().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
            Toast.makeText(getContext(), "Appointment added to calendar!", Toast.LENGTH_SHORT).show();
            scheduleNotifications(appointment, startMillis);
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Permission required to access calendar!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error adding event to calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotifications(Appointment appointment, long triggerTime) {
        int reminderTimeInMinutes = getReminderTimeInMinutes();
        if (reminderTimeInMinutes > 0) {
            Intent intent = new Intent(getContext(), NotificationReceiver.class);
            intent.putExtra("title", "Upcoming Appointment");
            intent.putExtra("message", "Appointment with Dr. " + appointment.getDoctorName() + " at " + appointment.getTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime - (reminderTimeInMinutes * 60 * 1000), pendingIntent);
            }
        }

        // Set multiple reminders for 1 hour, 1 day, and 1 week before
        setAdditionalReminders(appointment, triggerTime);
    }

    private int getReminderTimeInMinutes() {
        switch (selectedReminder) {
            case "15 minutes": return 15;
            case "30 minutes": return 30;
            case "1 hour": return 60;
            case "1 day": return 1440; // 1 day = 1440 minutes
            case "1 week": return 10080; // 1 week = 10080 minutes
            default: return 0;
        }
    }

    private void setAdditionalReminders(Appointment appointment, long triggerTime) {
        // Set reminder for 1 hour before
        scheduleReminder(appointment, triggerTime - (60 * 60 * 1000), "1 hour before");

        // Set reminder for 1 day before
        scheduleReminder(appointment, triggerTime - (24 * 60 * 60 * 1000), "1 day before");

        // Set reminder for 1 week before
        scheduleReminder(appointment, triggerTime - (7 * 24 * 60 * 60 * 1000), "1 week before");
    }

    private void scheduleReminder(Appointment appointment, long reminderTime, String label) {
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("title", "Upcoming Appointment");
        intent.putExtra("message", label + ": Appointment with Dr. " + appointment.getDoctorName() + " at " + appointment.getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateText.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                    timeText.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private boolean validateInputs() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(getContext(), "Please select a time.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (doctorInput.getText().toString().isEmpty()) {
            doctorInput.setError("Please enter the doctor's name.");
            return false;
        }
        if (typeInput.getText().toString().isEmpty()) {
            typeInput.setError("Please enter the appointment type.");
            return false;
        }


        return true;
    }
}