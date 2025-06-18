package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirstTimeLogin extends AppCompatActivity {
    private int height, weight, workouts;
    private boolean gender;
    private String username, userEmail;

    // New comprehensive fields
    private String fitnessGoals;
    private String fitnessLevel;
    private String previousExperience;
    private String physicalLimitations;
    private int availableDaysPerWeek;
    private int maxWorkoutDuration;
    private String availableEquipment;
    private String preferredWorkoutTypes;
    private String dislikedWorkoutTypes;
    private String dietaryPreferences;
    private String allergies;
    private String currentDiet;
    private int sleepHours;
    private String stressLevel;
    private String motivationFactors;
    private String previousInjuries;
    private boolean hasChronicConditions;
    private String activityLevel;

    // UI Components
    private EditText usernameEditText, weightEditText, heightEditText, workoutEditText;
    private EditText previousExperienceEditText, physicalLimitationsEditText, allergiesEditText;
    private EditText currentDietEditText, motivationFactorsEditText, previousInjuriesEditText;
    private RadioButton isMale, isFemale;
    private Spinner fitnessGoalsSpinner, fitnessLevelSpinner, availableEquipmentSpinner;
    private Spinner dietaryPreferencesSpinner, stressLevelSpinner, activityLevelSpinner;
    private SeekBar availableDaysSeekBar, maxWorkoutDurationSeekBar, sleepHoursSeekBar;
    private TextView availableDaysText, maxWorkoutDurationText, sleepHoursText;
    private CheckBox[] preferredWorkoutCheckBoxes, dislikedWorkoutCheckBoxes;
    private CheckBox hasChronicConditionsCheckBox;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_login);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail = currentUser.getEmail();
        }

        initializeViews();
        setupSpinners();
        setupSeekBars();
        setupSubmitButton();
    }

    private void initializeViews() {
        // Basic info (existing)
        usernameEditText = findViewById(R.id.editTextUsername);
        weightEditText = findViewById(R.id.editTextText3);
        heightEditText = findViewById(R.id.editTextText4);
        workoutEditText = findViewById(R.id.editTextText2);
        isMale = findViewById(R.id.radio_male);
        isFemale = findViewById(R.id.radio_female);

        // Add these missing initializations:
        previousExperienceEditText = findViewById(R.id.editTextPreviousExperience);
        physicalLimitationsEditText = findViewById(R.id.editTextPhysicalLimitations);
        previousInjuriesEditText = findViewById(R.id.editTextPreviousInjuries);
        currentDietEditText = findViewById(R.id.editTextCurrentDiet);
        allergiesEditText = findViewById(R.id.editTextAllergies);
        motivationFactorsEditText = findViewById(R.id.editTextMotivationFactors);

        // Spinners
        fitnessGoalsSpinner = findViewById(R.id.spinnerFitnessGoals);
        fitnessLevelSpinner = findViewById(R.id.spinnerFitnessLevel);
        activityLevelSpinner = findViewById(R.id.spinnerActivityLevel);
        dietaryPreferencesSpinner = findViewById(R.id.spinnerDietaryPreferences);
        stressLevelSpinner = findViewById(R.id.spinnerStressLevel);

        // SeekBars and their text views
        availableDaysSeekBar = findViewById(R.id.seekBarAvailableDays);
        maxWorkoutDurationSeekBar = findViewById(R.id.seekBarMaxWorkoutDuration);
        sleepHoursSeekBar = findViewById(R.id.seekBarSleepHours);
        availableDaysText = findViewById(R.id.textAvailableDays);
        maxWorkoutDurationText = findViewById(R.id.textMaxWorkoutDuration);
        sleepHoursText = findViewById(R.id.textSleepHours);

        submitButton = findViewById(R.id.button5);
    }
    private void setupSpinners() {
        // Fitness Goals
        String[] fitnessGoalsOptions = {"Lose Weight", "Build Muscle", "Improve Endurance", "Tone Body", "General Fitness", "Athletic Performance"};
        ArrayAdapter<String> fitnessGoalsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fitnessGoalsOptions);
        fitnessGoalsSpinner.setAdapter(fitnessGoalsAdapter);

        // Fitness Level
        String[] fitnessLevelOptions = {"Beginner", "Intermediate", "Advanced", "Athlete"};
        ArrayAdapter<String> fitnessLevelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fitnessLevelOptions);
        fitnessLevelSpinner.setAdapter(fitnessLevelAdapter);

        // Dietary Preferences
        String[] dietaryOptions = {"Omnivore", "Vegetarian", "Vegan", "Keto", "Paleo", "Mediterranean", "No Specific Diet"};
        ArrayAdapter<String> dietaryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dietaryOptions);
        dietaryPreferencesSpinner.setAdapter(dietaryAdapter);

        // Stress Level
        String[] stressOptions = {"Low", "Moderate", "High", "Very High"};
        ArrayAdapter<String> stressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stressOptions);
        stressLevelSpinner.setAdapter(stressAdapter);

        // Activity Level
        String[] activityOptions = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active"};
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityOptions);
        activityLevelSpinner.setAdapter(activityAdapter);
    }

    private void setupSeekBars() {
        // Available Days (1-7)
        availableDaysSeekBar.setMax(6); // 0-6 represents 1-7 days
        availableDaysSeekBar.setProgress(2); // Default 3 days
        availableDaysText.setText("3 days per week");
        availableDaysSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int days = progress + 1;
                availableDaysText.setText(days + " days per week");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Max Workout Duration (15-120 minutes)
        maxWorkoutDurationSeekBar.setMax(105); // 0-105 represents 15-120 minutes
        maxWorkoutDurationSeekBar.setProgress(30); // Default 45 minutes
        maxWorkoutDurationText.setText("45 minutes");
        maxWorkoutDurationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = progress + 15;
                maxWorkoutDurationText.setText(duration + " minutes");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Sleep Hours (4-12 hours)
        sleepHoursSeekBar.setMax(8); // 0-8 represents 4-12 hours
        sleepHoursSeekBar.setProgress(4); // Default 8 hours
        sleepHoursText.setText("8 hours");
        sleepHoursSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int hours = progress + 4;
                sleepHoursText.setText(hours + " hours");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateAndCollectData()) {
                saveUserData();
            }
        });
    }

    private boolean validateAndCollectData() {
        // ... existing basic validation code ...

        // Collect comprehensive data
        fitnessGoals = fitnessGoalsSpinner.getSelectedItem().toString();
        fitnessLevel = fitnessLevelSpinner.getSelectedItem().toString();
        activityLevel = activityLevelSpinner.getSelectedItem().toString();
        previousExperience = previousExperienceEditText.getText().toString().trim();
        physicalLimitations = physicalLimitationsEditText.getText().toString().trim();
        previousInjuries = previousInjuriesEditText.getText().toString().trim();
        availableDaysPerWeek = availableDaysSeekBar.getProgress() + 1;
        maxWorkoutDuration = maxWorkoutDurationSeekBar.getProgress() + 15;
        dietaryPreferences = dietaryPreferencesSpinner.getSelectedItem().toString();
        currentDiet = currentDietEditText.getText().toString().trim();
        allergies = allergiesEditText.getText().toString().trim();
        sleepHours = sleepHoursSeekBar.getProgress() + 4;
        stressLevel = stressLevelSpinner.getSelectedItem().toString();
        motivationFactors = motivationFactorsEditText.getText().toString().trim();

        return true;
    }

    private void saveUserData() {
        FirebaseHandler.saveFirstTimeUser(
                height, workouts, weight, gender, username, userEmail,
                fitnessGoals, fitnessLevel, previousExperience, physicalLimitations,
                availableDaysPerWeek, maxWorkoutDuration, availableEquipment,
                preferredWorkoutTypes, dislikedWorkoutTypes, dietaryPreferences,
                allergies, currentDiet, sleepHours, stressLevel,
                motivationFactors, previousInjuries, hasChronicConditions,
                activityLevel
        );
    }
}