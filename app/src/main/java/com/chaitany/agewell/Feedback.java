package com.chaitany.agewell;

import java.util.List;

public class Feedback {
    private String id;
    private String userName; // New field for user's name
    private int rating;
    private String comment;
    private long timestamp;
    private String userPhone;

    // Required empty constructor for Firebase
    public Feedback() {}

    public Feedback(String id, String userName, int rating, String comment, String userPhone) {
        this.id = id;
        this.userName = userName; // Initialize userName
        this.rating = rating;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis();
        this.userPhone = userPhone; // Store user phone for reference
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; } // Getter for userName
    public void setUserName(String userName) { this.userName = userName; } // Setter for userName

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
}