package com.example.myapplication3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingPlan extends AppCompatActivity {
    private boolean alertShown = false;
    private FirebaseHandler training;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userId = user.getUid();

    // Workout type spinners
    private Spinner spinnerSunday, spinnerMonday, spinnerTuesday, spinnerWednesday,
            spinnerThursday, spinnerFriday, spinnerSaturday;

    // Additional description EditTexts
    private EditText descSunday, descMonday, descTuesday, descWednesday,
            descThursday, descFriday, descSaturday;

    private Button resetButton, aiTipsButton;

    // Workout types for the dropdown
    private List<String> workoutTypes;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_plan);

        FirebaseHandler handler = new FirebaseHandler();
        handler.updateWorkoutFrequency();

        training = new FirebaseHandler();

        initializeWorkoutTypes();
        initializeViews();
        setupSpinners();
        setupTextWatchers();
        setupButtons();

        // Load workouts from Firebase
        loadData();
    }

    private void initializeWorkoutTypes() {
        workoutTypes = new ArrayList<>(Arrays.asList(
                "Rest Day",
                "Running",
                "Gym Training",
                "Swimming",
                "Cycling",
                "Yoga",
                "Pilates",
                "Rock Climbing",
                "Hiking",
                "Dancing",
                "Boxing",
                "Martial Arts",
                "Basketball",
                "Football",
                "Tennis",
                "Volleyball",
                "CrossFit",
                "Bodyweight Training",
                "Stretching",
                "Walking",
                "Jogging",
                "Weightlifting",
                "Cardio",
                "HIIT",
                "Zumba",
                "Rowing",
                "Elliptical",
                "Stair Climbing",
                "Jump Rope",
                "Strength Training"
        ));
    }

    private void initializeViews() {
        // Initialize spinners
        spinnerSunday = findViewById(R.id.spinner_sunday);
        spinnerMonday = findViewById(R.id.spinner_monday);
        spinnerTuesday = findViewById(R.id.spinner_tuesday);
        spinnerWednesday = findViewById(R.id.spinner_wednesday);
        spinnerThursday = findViewById(R.id.spinner_thursday);
        spinnerFriday = findViewById(R.id.spinner_friday);
        spinnerSaturday = findViewById(R.id.spinner_saturday);

        // Initialize description EditTexts
        descSunday = findViewById(R.id.desc_sunday);
        descMonday = findViewById(R.id.desc_monday);
        descTuesday = findViewById(R.id.desc_tuesday);
        descWednesday = findViewById(R.id.desc_wednesday);
        descThursday = findViewById(R.id.desc_thursday);
        descFriday = findViewById(R.id.desc_friday);
        descSaturday = findViewById(R.id.desc_saturday);

        // Initialize buttons
        resetButton = findViewById(R.id.resetButton);
        aiTipsButton = findViewById(R.id.aiTipsButton);
    }

    private void setupSpinners() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner[] spinners = {spinnerSunday, spinnerMonday, spinnerTuesday, spinnerWednesday,
                spinnerThursday, spinnerFriday, spinnerSaturday};

        for (Spinner spinner : spinners) {
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    saveData();
                    checkForRestDaySuggestion();
                    FirebaseHandler handler = new FirebaseHandler();
                    handler.updateWorkoutFrequency();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                saveData();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        EditText[] editTexts = {descSunday, descMonday, descTuesday, descWednesday,
                descThursday, descFriday, descSaturday};

        for (EditText editText : editTexts) {
            editText.addTextChangedListener(textWatcher);
        }
    }

    private void setupButtons() {
        resetButton.setOnClickListener(v -> {
            clearWorkouts();
            saveData();
        });

        aiTipsButton.setOnClickListener(v -> {
            getAIWorkoutTip();
        });
    }

    private void saveData() {
        Map<String, String> workoutData = new HashMap<>();

        workoutData.put("Sunday", getWorkoutString(spinnerSunday, descSunday));
        workoutData.put("Monday", getWorkoutString(spinnerMonday, descMonday));
        workoutData.put("Tuesday", getWorkoutString(spinnerTuesday, descTuesday));
        workoutData.put("Wednesday", getWorkoutString(spinnerWednesday, descWednesday));
        workoutData.put("Thursday", getWorkoutString(spinnerThursday, descThursday));
        workoutData.put("Friday", getWorkoutString(spinnerFriday, descFriday));
        workoutData.put("Saturday", getWorkoutString(spinnerSaturday, descSaturday));

        training.saveTrainingPlan(userId, workoutData);
    }

    private String getWorkoutString(Spinner spinner, EditText description) {
        String selectedWorkout = spinner.getSelectedItem().toString();
        String desc = description.getText().toString().trim();

        if (selectedWorkout.equals("Rest Day")) {
            return "Rest Day";
        }

        if (!desc.isEmpty()) {
            return selectedWorkout + ": " + desc;
        } else {
            return selectedWorkout;
        }
    }

    private void loadData() {
        training.getTrainingPlan(userId, new FirebaseHandler.FirebaseDataCallback() {
            @Override
            public void onDataReceived(Map<String, String> workouts) {
                if (workouts != null) {
                    setWorkoutFromString("Sunday", workouts.getOrDefault("Sunday", "Rest Day"), spinnerSunday, descSunday);
                    setWorkoutFromString("Monday", workouts.getOrDefault("Monday", "Rest Day"), spinnerMonday, descMonday);
                    setWorkoutFromString("Tuesday", workouts.getOrDefault("Tuesday", "Rest Day"), spinnerTuesday, descTuesday);
                    setWorkoutFromString("Wednesday", workouts.getOrDefault("Wednesday", "Rest Day"), spinnerWednesday, descWednesday);
                    setWorkoutFromString("Thursday", workouts.getOrDefault("Thursday", "Rest Day"), spinnerThursday, descThursday);
                    setWorkoutFromString("Friday", workouts.getOrDefault("Friday", "Rest Day"), spinnerFriday, descFriday);
                    setWorkoutFromString("Saturday", workouts.getOrDefault("Saturday", "Rest Day"), spinnerSaturday, descSaturday);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setWorkoutFromString(String day, String workoutString, Spinner spinner, EditText description) {
        if (workoutString.isEmpty() || workoutString.equals("Rest Day")) {
            spinner.setSelection(0); // Rest Day
            description.setText("");
            return;
        }

        // Check if the workout string contains a colon (indicating additional description)
        if (workoutString.contains(": ")) {
            String[] parts = workoutString.split(": ", 2);
            String workoutType = parts[0];
            String desc = parts[1];

            // Find the workout type in the spinner
            int position = workoutTypes.indexOf(workoutType);
            if (position >= 0) {
                spinner.setSelection(position);
                description.setText(desc);
            } else {
                // If workout type not found, add it to the list
                workoutTypes.add(workoutType);
                spinnerAdapter.notifyDataSetChanged();
                spinner.setSelection(workoutTypes.indexOf(workoutType));
                description.setText(desc);
            }
        } else {
            // No additional description, just set the workout type
            int position = workoutTypes.indexOf(workoutString);
            if (position >= 0) {
                spinner.setSelection(position);
                description.setText("");
            } else {
                // If workout type not found, add it to the list
                workoutTypes.add(workoutString);
                spinnerAdapter.notifyDataSetChanged();
                spinner.setSelection(workoutTypes.indexOf(workoutString));
                description.setText("");
            }
        }
    }

    private void clearWorkouts() {
        Spinner[] spinners = {spinnerSunday, spinnerMonday, spinnerTuesday, spinnerWednesday,
                spinnerThursday, spinnerFriday, spinnerSaturday};
        EditText[] editTexts = {descSunday, descMonday, descTuesday, descWednesday,
                descThursday, descFriday, descSaturday};

        for (Spinner spinner : spinners) {
            spinner.setSelection(0); // Set to "Rest Day"
        }

        for (EditText editText : editTexts) {
            editText.setText("");
        }
    }

    private void checkForRestDaySuggestion() {
        if (alertShown) return;

        Spinner[] spinners = {spinnerSunday, spinnerMonday, spinnerTuesday, spinnerWednesday,
                spinnerThursday, spinnerFriday, spinnerSaturday};

        boolean allWorkouts = true;
        for (Spinner spinner : spinners) {
            if (spinner.getSelectedItem().toString().equals("Rest Day")) {
                allWorkouts = false;
                break;
            }
        }

        if (allWorkouts) {
            alertShown = true;
            new AlertDialog.Builder(this)
                    .setTitle("Rest Day Suggestion")
                    .setMessage("You've scheduled workouts every day! Consider adding rest days.")
                    .setPositiveButton("OK", (dialog, which) -> {})
                    .show();
        }
    }

    private void getAIWorkoutTip() {
        // Get current workout data
        Map<String, String> workoutData = new HashMap<>();
        workoutData.put("Sunday", getWorkoutString(spinnerSunday, descSunday));
        workoutData.put("Monday", getWorkoutString(spinnerMonday, descMonday));
        workoutData.put("Tuesday", getWorkoutString(spinnerTuesday, descTuesday));
        workoutData.put("Wednesday", getWorkoutString(spinnerWednesday, descWednesday));
        workoutData.put("Thursday", getWorkoutString(spinnerThursday, descThursday));
        workoutData.put("Friday", getWorkoutString(spinnerFriday, descFriday));
        workoutData.put("Saturday", getWorkoutString(spinnerSaturday, descSaturday));

        // Check if there are any workouts to analyze
        boolean hasWorkouts = false;
        for (String workout : workoutData.values()) {
            if (!workout.trim().isEmpty() && !workout.equals("Rest Day")) {
                hasWorkouts = true;
                break;
            }
        }

        if (!hasWorkouts) {
            Toast.makeText(this, "Please add some workouts first to get AI tips!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading message
        Toast.makeText(this, "Getting AI tips for your workout plan...", Toast.LENGTH_SHORT).show();

        // Get user BMI for context
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int weight = snapshot.child("weight").getValue(Integer.class);
                    int height = snapshot.child("height").getValue(Integer.class);
                    double heightInMeters = height / 100.0;
                    double bmi = weight / (heightInMeters * heightInMeters);

                    // Build the prompt for Gemini based on workout plan and BMI
                    String prompt = buildGeminiPrompt(workoutData, bmi);

                    // Send to Gemini API
                    ChatCall.sendToGemini(prompt, new ChatCall.GeminiCallback() {
                        @Override
                        public void onTipReceived(String tip) {
                            // Show the Gemini workout tip in an alert dialog
                            new androidx.appcompat.app.AlertDialog.Builder(TrainingPlan.this)
                                    .setTitle("AI Workout Analysis")
                                    .setMessage(tip)
                                    .setPositiveButton("Thanks!", null)
                                    .show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(TrainingPlan.this, "Error getting AI tips: " + error, Toast.LENGTH_SHORT).show();
                            Log.e("GeminiAPI", "Error getting workout tip: " + error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainingPlan.this, "Error accessing user data", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }

    private String buildGeminiPrompt(Map<String, String> workoutData, double bmi) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on my workout plan and BMI of ").append(String.format("%.1f", bmi));
        prompt.append(", please analyze my workout routine and give me one specific improvement tip.\n\n");
        prompt.append("My current workout plan:\n");

        // Add each day's workout
        int restDays = 0;
        for (Map.Entry<String, String> entry : workoutData.entrySet()) {
            String day = entry.getKey();
            String workout = entry.getValue().trim();

            if (workout.isEmpty() || workout.equals("Rest Day")) {
                restDays++;
                prompt.append(day).append(": Rest day\n");
            } else {
                prompt.append(day).append(": ").append(workout).append("\n");
            }
        }

        // Add analysis hints based on workout patterns
        String workoutString = workoutData.values().toString().toLowerCase();

        // Check for workout types
        boolean hasCardio = workoutString.contains("running") || workoutString.contains("cycling") ||
                workoutString.contains("swimming") || workoutString.contains("cardio") ||
                workoutString.contains("hiit") || workoutString.contains("jogging");

        boolean hasStrength = workoutString.contains("gym training") || workoutString.contains("weightlifting") ||
                workoutString.contains("strength training") || workoutString.contains("bodyweight training") ||
                workoutString.contains("crossfit");

        boolean hasFlexibility = workoutString.contains("yoga") || workoutString.contains("pilates") ||
                workoutString.contains("stretching");

        // Add specific context to help Gemini provide better tips
        if (restDays == 0) {
            prompt.append("\nNote: I don't have any rest days currently scheduled.");
        } else if (restDays > 4) {
            prompt.append("\nNote: I have ").append(restDays).append(" rest days scheduled.");
        }

        if (!hasCardio) {
            prompt.append("\nNote: I don't seem to have any cardio workouts.");
        }
        if (!hasStrength) {
            prompt.append("\nNote: I don't seem to have any strength training workouts.");
        }
        if (!hasFlexibility) {
            prompt.append("\nNote: I don't seem to have any flexibility/mobility workouts.");
        }

        // BMI-specific considerations
        if (bmi > 30) {
            prompt.append("\nNote: With my higher BMI, I'm looking for effective but joint-friendly options.");
        } else if (bmi < 18.5) {
            prompt.append("\nNote: With my lower BMI, I'm interested in building muscle and strength.");
        }

        prompt.append("\nPlease give me one practical, specific tip to improve my workout plan. Keep it to 5-8 lines maximum.");

        return prompt.toString();
    }
}