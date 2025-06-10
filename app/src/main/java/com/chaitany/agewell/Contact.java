package com.chaitany.agewell;

public class Contact {
    private String id;
    private String name;
    private String phone;
    private String category;
    private int priority; // Priority: 1 for High, 0 for Normal

    public Contact() {
        // Default constructor for Firebase
    }

    public Contact(String id, String name, String phone, String category, int priority) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.category = category;
        this.priority = priority;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    // Custom methods for adapter
    public String getPriorityText() {
        // Returns the text to be displayed in the priority chip
        if (priority == 1) {
            return "High Priority";
        } else {
            return "Normal Priority";
        }
    }

    public boolean isHighPriority() {
        // Returns true if the contact has high priority
        return priority == 1;
    }
}
