package com.example.myapplication3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WeightTrack extends AppCompatActivity {

    private Button btnShowBMI, btnUpdateWeight, btnGetPersonalizedAdvice;
    private EditText editTextWeight;
    private boolean isRequestRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_track);

        btnShowBMI = findViewById(R.id.buttonShowBMI);
        btnUpdateWeight = findViewById(R.id.buttonUpdateWeight);
        btnGetPersonalizedAdvice = findViewById(R.id.buttonPersonalizedAdvice);
        editTextWeight = findViewById(R.id.editTextWeight);

        btnShowBMI.setOnClickListener(v -> showBMI());
        btnUpdateWeight.setOnClickListener(v -> updateUserWeight());
        btnGetPersonalizedAdvice.setOnClickListener(v -> getPersonalizedFitnessAdvice());
    }

    private void showBMI() {
        if (isRequestRunning) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        isRequestRunning = true;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(WeightTrack.this, "No user data found.", Toast.LENGTH_SHORT).show();
                    isRequestRunning = false;
                    return;
                }

                FirebaseHandler.User user = snapshot.getValue(FirebaseHandler.User.class);
                double bmi = calculateBMI(user.weight, user.height);
                String basicPrompt = createBasicBMIPrompt(user, bmi);

                ChatCall.sendToGemini(basicPrompt, new ChatCall.GeminiCallback() {
                    @Override
                    public void onTipReceived(String tip) {
                        isRequestRunning = false;
                        showBMIResultDialog(bmi, tip);
                    }

                    @Override
                    public void onError(String error) {
                        isRequestRunning = false;
                        Toast.makeText(WeightTrack.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isRequestRunning = false;
                Toast.makeText(WeightTrack.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPersonalizedFitnessAdvice() {
        if (isRequestRunning) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        isRequestRunning = true;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(WeightTrack.this, "No user data found.", Toast.LENGTH_SHORT).show();
                    isRequestRunning = false;
                    return;
                }

                FirebaseHandler.User userData = snapshot.getValue(FirebaseHandler.User.class);
                String comprehensivePrompt = createComprehensiveFitnessPrompt(userData);

                ChatCall.sendToGemini(comprehensivePrompt, new ChatCall.GeminiCallback() {
                    @Override
                    public void onTipReceived(String advice) {
                        isRequestRunning = false;
                        showPersonalizedAdviceDialog(advice);
                    }

                    @Override
                    public void onError(String error) {
                        isRequestRunning = false;
                        Toast.makeText(WeightTrack.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isRequestRunning = false;
                Toast.makeText(WeightTrack.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String createBasicBMIPrompt(FirebaseHandler.User user, double bmi) {
        String goal = getSelectedGoal();
        return String.format(
                "I am using a health tracking app. My gender is %s, my height is %d cm and weight is %d kg. " +
                        "I work out %d times per week. My goal is to %s weight. My BMI is %.2f. " +
                        "Can you give me a practical tip to get my weight to be healthier based on this?",
                user.gender, user.height, user.weight, user.workoutFrequency, goal.toLowerCase(), bmi
        );
    }

    private String createComprehensiveFitnessPrompt(FirebaseHandler.User user) {
        String goal = getSelectedGoal();
        double bmi = calculateBMI(user.weight, user.height);

        StringBuilder prompt = new StringBuilder();
        prompt.append("I want you to take on the role of a fitness expert, personal trainer, and sports nutritionist. ");
        prompt.append("I need personalized fitness and nutrition advice based on my comprehensive profile:\n\n");

        // Basic info
        prompt.append("BASIC INFORMATION:\n");
        prompt.append(String.format("- Gender: %s\n", user.gender));
        prompt.append(String.format("- Height: %d cm, Weight: %d kg (BMI: %.2f)\n", user.height, user.weight, bmi));
        prompt.append(String.format("- Current workout frequency: %d times per week\n", user.workoutFrequency));
        prompt.append(String.format("- Current goal: %s weight\n", goal));

        // Additional comprehensive information
        if (user.fitnessGoals != null && !user.fitnessGoals.isEmpty()) {
            prompt.append(String.format("- Specific fitness goals: %s\n", user.fitnessGoals));
        }
        if (user.fitnessLevel != null && !user.fitnessLevel.isEmpty()) {
            prompt.append(String.format("- Current fitness level: %s\n", user.fitnessLevel));
        }
        if (user.previousExperience != null && !user.previousExperience.isEmpty()) {
            prompt.append(String.format("- Previous exercise experience: %s\n", user.previousExperience));
        }

        prompt.append("\nPHYSICAL LIMITATIONS & HEALTH:\n");
        if (user.physicalLimitations != null && !user.physicalLimitations.isEmpty()) {
            prompt.append(String.format("- Physical limitations: %s\n", user.physicalLimitations));
        }
        if (user.previousInjuries != null && !user.previousInjuries.isEmpty()) {
            prompt.append(String.format("- Previous injuries: %s\n", user.previousInjuries));
        }
        prompt.append(String.format("- Has chronic conditions: %s\n", user.hasChronicConditions ? "Yes" : "No"));

        prompt.append("\nTIME & EQUIPMENT AVAILABILITY:\n");
        if (user.availableDaysPerWeek > 0) {
            prompt.append(String.format("- Available days per week: %d\n", user.availableDaysPerWeek));
        }
        if (user.maxWorkoutDuration > 0) {
            prompt.append(String.format("- Maximum workout duration: %d minutes\n", user.maxWorkoutDuration));
        }
        if (user.availableEquipment != null && !user.availableEquipment.isEmpty()) {
            prompt.append(String.format("- Available equipment: %s\n", user.availableEquipment));
        }

        prompt.append("\nWORKOUT PREFERENCES:\n");
        if (user.preferredWorkoutTypes != null && !user.preferredWorkoutTypes.isEmpty()) {
            prompt.append(String.format("- Preferred workout types: %s\n", user.preferredWorkoutTypes));
        }
        if (user.dislikedWorkoutTypes != null && !user.dislikedWorkoutTypes.isEmpty()) {
            prompt.append(String.format("- Disliked workout types: %s\n", user.dislikedWorkoutTypes));
        }

        prompt.append("\nNUTRITION & LIFESTYLE:\n");
        if (user.dietaryPreferences != null && !user.dietaryPreferences.isEmpty()) {
            prompt.append(String.format("- Dietary preferences: %s\n", user.dietaryPreferences));
        }
        if (user.allergies != null && !user.allergies.isEmpty()) {
            prompt.append(String.format("- Food allergies: %s\n", user.allergies));
        }
        if (user.currentDiet != null && !user.currentDiet.isEmpty()) {
            prompt.append(String.format("- Current diet description: %s\n", user.currentDiet));
        }
        if (user.sleepHours > 0) {
            prompt.append(String.format("- Average sleep hours: %d\n", user.sleepHours));
        }
        if (user.stressLevel != null && !user.stressLevel.isEmpty()) {
            prompt.append(String.format("- Stress level: %s\n", user.stressLevel));
        }
        if (user.activityLevel != null && !user.activityLevel.isEmpty()) {
            prompt.append(String.format("- Daily activity level: %s\n", user.activityLevel));
        }

        prompt.append("\nMOTIVATION:\n");
        if (user.motivationFactors != null && !user.motivationFactors.isEmpty()) {
            prompt.append(String.format("- What motivates me: %s\n", user.motivationFactors));
        }

        prompt.append("\nBased on this comprehensive profile, please provide me with:\n");
        prompt.append("1. Specific recommendations for improving my weight/body composition\n");
        prompt.append("2. Nutrition guidelines that align with my goals and preferences\n");
        prompt.append("3. Workout suggestions that fit my limitations and preferences\n");
        prompt.append("4. Lifestyle tips considering my sleep and stress levels\n");
        prompt.append("5. Realistic timeline expectations for my goals\n\n");
        prompt.append("Please make your advice practical, safe, and achievable based on my specific situation. ");
        prompt.append("Keep the response concise but comprehensive (7-12 lines).");

        return prompt.toString();
    }

    private double calculateBMI(int weight, int height) {
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    private void showBMIResultDialog(double bmi, String tip) {
        new AlertDialog.Builder(WeightTrack.this)
                .setTitle("BMI Analysis")
                .setMessage("Your BMI: " + String.format("%.2f", bmi) + "\n\nRecommendation: " + tip)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPersonalizedAdviceDialog(String advice) {
        new AlertDialog.Builder(WeightTrack.this)
                .setTitle("Personalized Fitness Advice")
                .setMessage(advice)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateUserWeight() {
        String weightStr = editTextWeight.getText().toString().trim();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter a weight to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int newWeight = Integer.parseInt(weightStr);
            if (newWeight <= 0 || newWeight > 300) {
                Toast.makeText(this, "Please enter a valid weight (1-300 kg).", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseHandler.updateWeight(newWeight);
            editTextWeight.setText("");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedGoal() {
        RadioButton radioLose = findViewById(R.id.radioButton2);
        RadioButton radioStay = findViewById(R.id.radioButton);
        RadioButton radioBulk = findViewById(R.id.radioButton3);

        if (radioLose.isChecked()) {
            return "lose";
        } else if (radioStay.isChecked()) {
            return "maintain";
        } else if (radioBulk.isChecked()) {
            return "gain";
        } else {
            return "improve";
        }
    }
}