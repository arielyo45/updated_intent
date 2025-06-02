// WorkoutBuddy.java
package com.example.myapplication3;

public class Friend {
    private String userId;
    private String gender;
    private String username;

    private int workoutFrequency;
    private String workout;
    private String day;

    public Friend(String userId, String gender, int workoutFrequency, String workout, String day, String username) {
        this.userId = userId;
        this.gender = gender;
        this.workoutFrequency = workoutFrequency;
        this.workout = workout;
        this.day = day;
        this.username = username;
    }


    public String getUsername() {
        return username != null && !username.isEmpty() ? username : getDisplayName();
    }

    public String getUserId() { return userId; }
    public String getGender() { return gender; }
    public int getWorkoutFrequency() { return workoutFrequency; }
    public String getWorkout() { return workout; }
    public String getDay() { return day; }

    public String getDisplayName() {
        return username != null && !username.isEmpty() ? username : (gender + " Trainee #" + userId.substring(0, 6));
    }
}
