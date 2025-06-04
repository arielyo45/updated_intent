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

import org.json.JSONException;

public class WeightTrack extends AppCompatActivity {

    private Button btnShowBMI, btnUpdateWeight;
    private EditText editTextWeight;
    private boolean isRequestRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_track);

        btnShowBMI = findViewById(R.id.buttonShowBMI);
        btnUpdateWeight = findViewById(R.id.buttonUpdateWeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        btnShowBMI.setOnClickListener(v -> showBMI());
        btnUpdateWeight.setOnClickListener(v -> updateUserWeight());
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
                int weight = user.weight;
                int height = user.height;
                double heightInMeters = height / 100.0;
                double bmi = weight / (heightInMeters * heightInMeters);
                String gender = user.gender;
                String goal = getSelectedGoal();
                int workouts = user.workoutFrequency;
                String prompt = String.format(
                        "I am using a health tracking app. My gender is %s, my height is %d cm and weight is %d kg. " +
                                "I work out %d times per week. My goal is to %s weight. My BMI is %.2f. " +
                                "Can you give me a practical tip to get my weight to be healthier based on this?",
                        gender, height, weight, workouts, goal.toLowerCase(), bmi
                );

                ChatCall.sendToGemini(prompt, new ChatCall.GeminiCallback() {
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
    private void showBMIResultDialog(double bmi, String tip) {
        new AlertDialog.Builder(WeightTrack.this)
                .setTitle("BMI Result")
                .setMessage("BMI: " + String.format("%.2f", bmi) + "\nTip: " + tip)
                .setPositiveButton("OK", null)
                .show();
    }


    private void updateUserWeight() {
        String weightStr = editTextWeight.getText().toString().trim();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Enter a weight to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        int newWeight = Integer.parseInt(weightStr);
        FirebaseHandler.updateWeight( newWeight);
    }

    private String getSelectedGoal() {
        RadioButton radioLose = findViewById(R.id.radioButton2);
        RadioButton radioStay = findViewById(R.id.radioButton);
        RadioButton radioBulk = findViewById(R.id.radioButton3);

        if (radioLose.isChecked()) {
            return "Lose";
        } else if (radioStay.isChecked()) {
            return "Stay";
        } else if (radioBulk.isChecked()) {
            return "Bulk";
        } else {
            return "Unknown";
        }
    }

}
