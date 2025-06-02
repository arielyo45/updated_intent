// Friend.java
package com.example.myapplication3;

public class Friend {
    private String userId;
    private String gender;
    private String username;
    private String email;
    private int workoutFrequency;
    private String workout;
    private String day;

    public Friend(String userId, String gender, int workoutFrequency, String workout, String day, String username, String email) {
        this.userId = userId;
        this.gender = gender;
        this.workoutFrequency = workoutFrequency;
        this.workout = workout;
        this.day = day;
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username != null && !username.isEmpty() ? username : getDisplayName();
    }

    public String getUserId() { return userId; }
    public String getGender() { return gender; }
    public String getEmail() { return email; }
    public int getWorkoutFrequency() { return workoutFrequency; }
    public String getWorkout() { return workout; }
    public String getDay() { return day; }

    public String getDisplayName() {
        return username != null && !username.isEmpty() ? username : (gender + " Trainee #" + userId.substring(0, 6));
    }
}