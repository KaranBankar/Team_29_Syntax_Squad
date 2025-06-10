package com.chaitany.agewell;

import java.io.Serializable;

public class Exercise implements Serializable {
    private Long id;
    private String name;
    private String level;
    private int sets;
    private int reps;
    private int restTime;
    private String instructions;
    private String safetyTips;
    private String videoUrl;
    private boolean isCompleted;

    // Default constructor required for calls to DataSnapshot.getValue(Exercise.class)
    public Exercise() {
        this.id = System.currentTimeMillis(); // Use current time as a unique ID (example)
    }

    // Constructor with ID, to provide full details when available
    public Exercise(Long id, String name, String level, int sets, int reps, int restTime,
                    String instructions, String safetyTips, String videoUrl) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.instructions = instructions;
        this.safetyTips = safetyTips;
        this.videoUrl = videoUrl;
        this.isCompleted = false;
    }

    // Constructor without ID, uses default id generation if not provided
    public Exercise(String name, String level, int sets, int reps, int restTime,
                    String instructions, String safetyTips, String videoUrl) {
        this.id = System.currentTimeMillis(); // Generate unique ID using current timestamp
        this.name = name;
        this.level = level;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.instructions = instructions;
        this.safetyTips = safetyTips;
        this.videoUrl = videoUrl;
        this.isCompleted = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        if (sets >= 0) {
            this.sets = sets;
        }
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        if (reps >= 0) {
            this.reps = reps;
        }
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        if (restTime >= 0) {
            this.restTime = restTime;
        }
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSafetyTips() {
        return safetyTips;
    }

    public void setSafetyTips(String safetyTips) {
        this.safetyTips = safetyTips;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isCompleted() { // Fixed method name
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // Additional Methods
    public void markAsCompleted() {
        this.isCompleted = true;
    }

    public int getTotalDuration() {
        // Approximate total duration (excluding exercise time): rest time * (sets - 1)
        return restTime * (sets - 1);
    }

    @Override
    public String toString() {
        return "Exercise{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level='" + level + '\'' +
                ", sets=" + sets +
                ", reps=" + reps +
                ", restTime=" + restTime +
                ", instructions='" + instructions + '\'' +
                ", safetyTips='" + safetyTips + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}