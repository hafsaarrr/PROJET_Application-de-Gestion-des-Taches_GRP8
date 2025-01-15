package com.example.taskmanager2;

public class Task {
    private int id;
    private String title;
    private String description;
    private long dueDate;
    private int dueTimeInMinutes; // New field for time storage
    private int priority;
    private String category;
    private boolean isCompleted;

    public Task() {}

    public Task(String title, String description, long dueDate, int dueTimeInMinutes, int priority, String category) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTimeInMinutes = dueTimeInMinutes;
        this.priority = priority;
        this.category = category;
        this.isCompleted = false;
    }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getDueTimeInMinutes() { return dueTimeInMinutes; }
    public void setDueTimeInMinutes(int dueTimeInMinutes) {
        // Ensure time stays within 24-hour bounds (0-1439 minutes)
        this.dueTimeInMinutes = dueTimeInMinutes % 1440;
    }

    // Helper methods for time management
    public int getDueHours() {
        return dueTimeInMinutes / 60;
    }

    public int getDueMinutes() {
        return dueTimeInMinutes % 60;
    }

    public void setTime(int hours, int minutes) {
        // Validate hours and minutes
        if (hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60) {
            this.dueTimeInMinutes = (hours * 60) + minutes;
        } else {
            throw new IllegalArgumentException("Invalid time values. Hours must be 0-23 and minutes 0-59");
        }
    }


    // Optional: Add a method to get formatted time string
    public String getFormattedTime() {
        int hours = getDueHours();
        int minutes = getDueMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }

    // Toggle completion status
    public void toggleCompleted() {
        this.isCompleted = !this.isCompleted;
    }
}
