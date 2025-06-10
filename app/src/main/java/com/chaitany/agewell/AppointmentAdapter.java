package com.chaitany.agewell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private List<Appointment> appointments;
    private Context context;

    public AppointmentAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);

        // Parse the date to get day and month
        String[] dateParts = appointment.getDate().split("-");
        String day = dateParts[2];
        String month = getMonthAbbreviation(Integer.parseInt(dateParts[1]));

        holder.dayText.setText(day);
        holder.monthText.setText(month);
        holder.typeText.setText(appointment.getType());
        holder.doctorText.setText(appointment.getDoctorName());
        holder.timeText.setText(appointment.getTime());
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    private String getMonthAbbreviation(int month) {
        String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        return months[month - 1];
    }

    public void updateAppointments(List<Appointment> newAppointments) {
        appointments = newAppointments;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText, monthText, typeText, doctorText, timeText;

        public ViewHolder(View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.text_day);
            monthText = itemView.findViewById(R.id.text_month);
            typeText = itemView.findViewById(R.id.text_type);
            doctorText = itemView.findViewById(R.id.text_doctor);
            timeText = itemView.findViewById(R.id.text_time);
        }
    }
}