package com.chaitany.agewell;

public class Appointment {
    private long id;
    private String date;
    private String time;
    private String doctorName;
    private String type;

    public Appointment(long id, String date, String time, String doctorName, String type) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.doctorName = doctorName;
        this.type = type;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}