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
        setContentView(R.layout.activity_first_time_login_enhanced);

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
        // Basic info
        usernameEditText = findViewById(R.id.editTextUsername);
        weightEditText = findViewById(R.id.editTextText3);
        heightEditText = findViewById(R.id.editTextText4);
        workoutEditText = findViewById(R.id.editTextText2);
        isMale = findViewById(R.id.radio_male);
        isFemale = findViewById(R.id.radio_female);

        // New comprehensive fields
        previousExperienceEditText = findViewById(R.id.editTextPreviousExperience);
        physicalLimitationsEditText = findViewById(R.id.editTextPhysicalLimitations);
        allergiesEditText = findViewById(R.id.editTextAllergies);
        currentDietEditText = findViewById(R.id.editTextCurrentDiet);
        motivationFactorsEditText = findViewById(R.id.editTextMotivationFactors);
        previousInjuriesEditText = findViewById(R.id.editTextPreviousInjuries);

        // Spinners
        fitnessGoalsSpinner = findViewById(R.id.spinnerFitnessGoals);
        fitnessLevelSpinner = findViewById(R.id.spinnerFitnessLevel);
        availableEquipmentSpinner = findViewById(R.id.spinnerAvailableEquipment);
        dietaryPreferencesSpinner = findViewById(R.id.spinnerDietaryPreferences);
        stressLevelSpinner = findViewById(R.id.spinnerStressLevel);
        activityLevelSpinner = findViewById(R.id.spinnerActivityLevel);

        // SeekBars and their text views
        availableDaysSeekBar = findViewById(R.id.seekBarAvailableDays);
        maxWorkoutDurationSeekBar = findViewById(R.id.seekBarMaxWorkoutDuration);
        sleepHoursSeekBar = findViewById(R.id.seekBarSleepHours);
        availableDaysText = findViewById(R.id.textAvailableDays);
        maxWorkoutDurationText = findViewById(R.id.textMaxWorkoutDuration);
        sleepHoursText = findViewById(R.id.textSleepHours);

        // CheckBoxes for workout preferences
        preferredWorkoutCheckBoxes = new CheckBox[]{
                findViewById(R.id.checkBoxYoga),
                findViewById(R.id.checkBoxPilates),
                findViewById(R.id.checkBoxRunning),
                findViewById(R.id.checkBoxWeightlifting),
                findViewById(R.id.checkBoxHIIT),
                findViewById(R.id.checkBoxSwimming)
        };

        dislikedWorkoutCheckBoxes = new CheckBox[]{
                findViewById(R.id.checkBoxDislikeYoga),
                findViewById(R.id.checkBoxDislikePilates),
                findViewById(R.id.checkBoxDislikeRunning),
                findViewById(R.id.checkBoxDislikeWeightlifting),
                findViewById(R.id.checkBoxDislikeHIIT),
                findViewById(R.id.checkBoxDislikeSwimming)
        };

        hasChronicConditionsCheckBox = findViewById(R.id.checkBoxChronicConditions);
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

        // Available Equipment
        String[] equipmentOptions = {"Full Gym", "Home - Full Equipment", "Home - Basic Equipment", "Bodyweight Only", "Outdoor Equipment"};
        ArrayAdapter<String> equipmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, equipmentOptions);
        availableEquipmentSpinner.setAdapter(equipmentAdapter);

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
        try {
            // Basic validation
            weight = Integer.parseInt(weightEditText.getText().toString().trim());
            height = Integer.parseInt(heightEditText.getText().toString().trim());
            workouts = Integer.parseInt(workoutEditText.getText().toString().trim());
            username = usernameEditText.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (isMale.isChecked()) {
                gender = true;
            } else if (isFemale.isChecked()) {
                gender = false;
            } else {
                Toast.makeText(this, "Please select your gender!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (workouts < 0 || workouts > 14) {
                Toast.makeText(this, "Please enter a valid number of workouts (0-14)!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (weight < 30 || weight > 300) {
                Toast.makeText(this, "Please enter a valid weight (30-300 kg)!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (height < 100 || height > 250) {
                Toast.makeText(this, "Please enter a valid height (100-250 cm)!", Toast.LENGTH_SHORT).show();
                return false;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Collect comprehensive data
        fitnessGoals = fitnessGoalsSpinner.getSelectedItem().toString();
        fitnessLevel = fitnessLevelSpinner.getSelectedItem().toString();
        previousExperience = previousExperienceEditText.getText().toString().trim();
        physicalLimitations = physicalLimitationsEditText.getText().toString().trim();
        availableDaysPerWeek = availableDaysSeekBar.getProgress() + 1;
        maxWorkoutDuration = maxWorkoutDurationSeekBar.getProgress() + 15;
        availableEquipment = availableEquipmentSpinner.getSelectedItem().toString();

        // Collect preferred workout types
        StringBuilder preferredBuilder = new StringBuilder();
        String[] workoutTypes = {"Yoga", "Pilates", "Running", "Weightlifting", "HIIT", "Swimming"};
        for (int i = 0; i < preferredWorkoutCheckBoxes.length; i++) {
            if (preferredWorkoutCheckBoxes[i].isChecked()) {
                if (preferredBuilder.length() > 0) preferredBuilder.append(", ");
                preferredBuilder.append(workoutTypes[i]);
            }
        }
        preferredWorkoutTypes = preferredBuilder.toString();

        // Collect disliked workout types
        StringBuilder dislikedBuilder = new StringBuilder();
        for (int i = 0; i < dislikedWorkoutCheckBoxes.length; i++) {
            if (dislikedWorkoutCheckBoxes[i].isChecked()) {
                if (dislikedBuilder.length() > 0) dislikedBuilder.append(", ");
                dislikedBuilder.append(workoutTypes[i]);
            }
        }
        dislikedWorkoutTypes = dislikedBuilder.toString();

        dietaryPreferences = dietaryPreferencesSpinner.getSelectedItem().toString();
        allergies = allergiesEditText.getText().toString().trim();
        currentDiet = currentDietEditText.getText().toString().trim();
        sleepHours = sleepHoursSeekBar.getProgress() + 4;
        stressLevel = stressLevelSpinner.getSelectedItem().toString();
        motivationFactors = motivationFactorsEditText.getText().toString().trim();
        previousInjuries = previousInjuriesEditText.getText().toString().trim();
        hasChronicConditions = hasChronicConditionsCheckBox.isChecked();
        activityLevel = activityLevelSpinner.getSelectedItem().toString();

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