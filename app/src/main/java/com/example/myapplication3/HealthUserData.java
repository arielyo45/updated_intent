package com.example.myapplication3;

public class HealthUserData {
    private int age;
    private String gender;
    private double weight;
    private double height;
    private String goal;
    private int workoutsPerWeek;

    public HealthUserData(int age, String gender, double weight, double height, int workoutsPerWeek) {
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.goal = goal;
        this.workoutsPerWeek = workoutsPerWeek;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeight() {
        return height;
    }

    public String getGoal() {
        return goal;
    }

    public int getWorkoutsPerWeek() {
        return workoutsPerWeek;
    }
}
