// WorkoutBuddy.java
package com.example.myapplication3;

public class Friend {
    private String userId;
    private String gender;
    private int workoutFrequency;
    private String workout;
    private String day;

    public Friend(String userId, String gender, int workoutFrequency, String workout, String day) {
        this.userId = userId;
        this.gender = gender;
        this.workoutFrequency = workoutFrequency;
        this.workout = workout;
        this.day = day;
    }

    public String getUserId() { return userId; }
    public String getGender() { return gender; }
    public int getWorkoutFrequency() { return workoutFrequency; }
    public String getWorkout() { return workout; }
    public String getDay() { return day; }

    // Generate anonymous display name
    public String getDisplayName() {
        return gender + " Trainee #" + userId.substring(0, 6);
    }
}
