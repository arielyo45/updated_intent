package com.example.myapplication3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WeightTrack extends AppCompatActivity {

    private Button btnShowBMI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_track);

        btnShowBMI = findViewById(R.id.buttonShowBMI);


        btnShowBMI.setOnClickListener(v -> showBMI());
    }

    private void showBMI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

                    // Determine the selected goal
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
        goal = getSelectedGoal();
        if (goal.equals("Lose")) {
            if (bmi > 25) return "Consider a calorie deficit and regular exercise.";
            return "You're already in a good range!";
        } else if (goal.equals("Bulk")) {
            if (bmi < 18.5) return "Increase your calorie intake and strength training.";
            return "You're at a good weight for now!";
        } else { // Maintain weight
            return "Stick to a balanced diet and regular activity!";
        }
    }
}
