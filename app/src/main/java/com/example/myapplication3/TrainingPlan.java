package com.example.myapplication3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class TrainingPlan extends AppCompatActivity {
    private String bip;
    private String tip;
    private boolean alertShown = false;
    private FirebaseHandler training;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userId = user.getUid();

    private EditText workoutSunday, workoutMonday, workoutTuesday, workoutWednesday,
            workoutThursday, workoutFriday, workoutSaturday;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_plan);


        training = new FirebaseHandler();

        workoutSunday = findViewById(R.id.workout_sunday);
        workoutMonday = findViewById(R.id.workout_monday);
        workoutTuesday = findViewById(R.id.workout_tuesday);
        workoutWednesday = findViewById(R.id.workout_wednesday);
        workoutThursday = findViewById(R.id.workout_thursday);
        workoutFriday = findViewById(R.id.workout_friday);
        workoutSaturday = findViewById(R.id.workout_saturday);
        resetButton = findViewById(R.id.resetButton);

        // Load workouts from Firebase
        loadData();

        // Save data on text change
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                saveData();
                checkForRestDaySuggestion();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        workoutSunday.addTextChangedListener(textWatcher);
        workoutMonday.addTextChangedListener(textWatcher);
        workoutTuesday.addTextChangedListener(textWatcher);
        workoutWednesday.addTextChangedListener(textWatcher);
        workoutThursday.addTextChangedListener(textWatcher);
        workoutFriday.addTextChangedListener(textWatcher);
        workoutSaturday.addTextChangedListener(textWatcher);

        resetButton.setOnClickListener(v -> {
            clearWorkouts();
            saveData();
        });
    }

    private void saveData() {
        Map<String, String> workoutData = new HashMap<>();
        workoutData.put("Sunday", workoutSunday.getText().toString());
        workoutData.put("Monday", workoutMonday.getText().toString());
        workoutData.put("Tuesday", workoutTuesday.getText().toString());
        workoutData.put("Wednesday", workoutWednesday.getText().toString());
        workoutData.put("Thursday", workoutThursday.getText().toString());
        workoutData.put("Friday", workoutFriday.getText().toString());
        workoutData.put("Saturday", workoutSaturday.getText().toString());

        training.saveTrainingPlan(userId, workoutData);
    }

    private void loadData() {
        training.getTrainingPlan(userId, new FirebaseHandler.FirebaseDataCallback() {
            @Override
            public void onDataReceived(Map<String, String> workouts) {
                if (workouts != null) {
                    workoutSunday.setText(workouts.getOrDefault("Sunday", ""));
                    workoutMonday.setText(workouts.getOrDefault("Monday", ""));
                    workoutTuesday.setText(workouts.getOrDefault("Tuesday", ""));
                    workoutWednesday.setText(workouts.getOrDefault("Wednesday", ""));
                    workoutThursday.setText(workouts.getOrDefault("Thursday", ""));
                    workoutFriday.setText(workouts.getOrDefault("Friday", ""));
                    workoutSaturday.setText(workouts.getOrDefault("Saturday", ""));

                    // Now that we have the workout data, get the Gemini tip
                    getGeminiWorkoutTip(workouts);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void clearWorkouts() {
        workoutSunday.setText("");
        workoutMonday.setText("");
        workoutTuesday.setText("");
        workoutWednesday.setText("");
        workoutThursday.setText("");
        workoutFriday.setText("");
        workoutSaturday.setText("");
    }

    private void checkForRestDaySuggestion() {
        if (alertShown) return;

        boolean allFilled = !workoutSunday.getText().toString().isEmpty() &&
                !workoutMonday.getText().toString().isEmpty() &&
                !workoutTuesday.getText().toString().isEmpty() &&
                !workoutWednesday.getText().toString().isEmpty() &&
                !workoutThursday.getText().toString().isEmpty() &&
                !workoutFriday.getText().toString().isEmpty() &&
                !workoutSaturday.getText().toString().isEmpty();

        if (allFilled) {
            alertShown = true;
            new AlertDialog.Builder(this)
                    .setTitle("Rest Day Suggestion")
                    .setMessage("You've scheduled workouts every day! Consider adding rest days.")
                    .setPositiveButton("OK", (dialog, which) ->{})
                    .show();
        }
    }

    private void getGeminiWorkoutTip(Map<String, String> workoutData) {
        // Only proceed if we have some workout data to analyze
        if (workoutData == null || workoutData.isEmpty()) {
            return;
        }

        // Only proceed if there's at least one workout entered
        boolean hasWorkouts = false;
        for (String workout : workoutData.values()) {
            if (!workout.trim().isEmpty()) {
                hasWorkouts = true;
                break;
            }
        }

        if (!hasWorkouts) {
            return; // No workouts to analyze yet
        }

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
                            // Silently log the error without bothering the user
                            Log.e("GeminiAPI", "Error getting workout tip: " + error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently handle the error
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
        int emptyDays = 0;
        for (Map.Entry<String, String> entry : workoutData.entrySet()) {
            String day = entry.getKey();
            String workout = entry.getValue().trim();

            if (workout.isEmpty()) {
                emptyDays++;
                prompt.append(day).append(": Rest day\n");
            } else {
                prompt.append(day).append(": ").append(workout).append("\n");
            }
        }

        // Add analysis hints based on workout patterns
        String workoutString = workoutData.values().toString().toLowerCase();

        // Check for workout types
        boolean hasCardio = workoutString.contains("cardio") || workoutString.contains("run") ||
                workoutString.contains("jog") || workoutString.contains("swim");
        boolean hasStrength = workoutString.contains("weight") || workoutString.contains("strength") ||
                workoutString.contains("lift") || workoutString.contains("muscle");
        boolean hasFlexibility = workoutString.contains("stretch") || workoutString.contains("yoga") ||
                workoutString.contains("flexibility");

        // Add specific context to help Gemini provide better tips
        if (emptyDays == 0) {
            prompt.append("\nNote: I don't have any rest days currently scheduled.");
        } else if (emptyDays > 4) {
            prompt.append("\nNote: I have ").append(emptyDays).append(" rest days scheduled.");
        }

        if (!hasCardio && !hasStrength && !hasFlexibility) {
            prompt.append("\nNote: My workout descriptions are basic. Please suggest specific workout types.");
        } else {
            if (!hasCardio) {
                prompt.append("\nNote: I don't seem to have any cardio workouts.");
            }
            if (!hasStrength) {
                prompt.append("\nNote: I don't seem to have any strength training workouts.");
            }
            if (!hasFlexibility) {
                prompt.append("\nNote: I don't seem to have any flexibility/mobility workouts.");
            }
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