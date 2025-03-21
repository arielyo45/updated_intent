package com.example.myapplication3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication3.FirebaseHandler;
import com.example.myapplication3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WeightTrack extends AppCompatActivity {

    private Button btnShowBMI, btnUpdateWeight;
    private EditText editTextWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_track);

        btnShowBMI = findViewById(R.id.buttonShowBMI);
        btnUpdateWeight = findViewById(R.id.buttonUpdateWeight);
        editTextWeight = findViewById(R.id.editTextWeight);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(this,"use: " + user.getUid(), Toast.LENGTH_SHORT).show();
        if (user != null) {

            FirebaseHandler.getData(user.getUid(), this);
        }
        else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }

        btnShowBMI.setOnClickListener(v -> showBMI());
        btnUpdateWeight.setOnClickListener(v -> updateUserWeight());
    }

    private void showBMI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int weight = snapshot.child("weight").getValue(Integer.class);
                    int height = snapshot.child("height").getValue(Integer.class);
                    double heightInMeters = height / 100.0;
                    double bmi = weight / (heightInMeters * heightInMeters);

                    String goal = getSelectedGoal();
                    String tip = getBMIBasedTip(bmi, goal);

                    new AlertDialog.Builder(WeightTrack.this)
                            .setTitle("BMI Result")
                            .setMessage("BMI: " + String.format("%.2f", bmi) + "\nTip: " + tip)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeightTrack.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserWeight() {
        String weightStr = editTextWeight.getText().toString().trim();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Enter a weight to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        int newWeight = Integer.parseInt(weightStr);
        FirebaseHandler.updateWeight(this, newWeight);
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

    private String getBMIBasedTip(double bmi, String goal) {
        if (goal.equals("Lose")) {
            return (bmi > 25) ? "Consider a calorie deficit and regular exercise." : "You're already in a good range!";
        } else if (goal.equals("Bulk")) {
            return (bmi < 18.5) ? "Increase your calorie intake and strength training." : "You're at a good weight for now!";
        } else {
            return "Stick to a balanced diet and regular activity!";
        }
    }
}
