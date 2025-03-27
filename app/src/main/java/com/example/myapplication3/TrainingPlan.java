package com.example.myapplication3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.Calendar;
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

        showBMI();

        training = new FirebaseHandler();

        // Link UI elements
        workoutSunday = findViewById(R.id.workout_sunday);
        workoutMonday = findViewById(R.id.workout_monday);
        workoutTuesday = findViewById(R.id.workout_tuesday);
        workoutWednesday = findViewById(R.id.workout_wednesday);
        workoutThursday = findViewById(R.id.workout_thursday);
        workoutFriday = findViewById(R.id.workout_friday);
        workoutSaturday = findViewById(R.id.workout_saturday);
        resetButton = findViewById(R.id.resetButton);

        // Load saved workouts from Firebase
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
                    .setPositiveButton("OK", (dialog, which) -> alertShown = false)
                    .show();
        }
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
                    tip = gettrainingtips(bmi);



                    new androidx.appcompat.app.AlertDialog.Builder(TrainingPlan.this)
                            .setTitle("Training tip")
                            .setMessage(  "\nTip: " + tip)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
            public  String gettrainingtips(double bmi){


                if(bmi>=40){bip = "I highly recommend you to get as many workout days as possible especially cardio workouts as long as you do it proggresivly. ";}
                 if(bmi>=30&&bmi<40){bip = "training cardio is reccomended, gradually increase the intesity of the workouts and youll be good";}
                if(bmi>=25 && bmi <30){ bip = "aim to train your strength and gradually increase your cardio workouts";}
                if(bmi<25&& bmi>=18.5){bip = "aim to balance cardio with strength training to maintain muscle tone, boost metabolism, and stay overall fit.";}
                if(bmi<18.5){ bip = "training is not highly reccomended if you do decide to workout put your focus on strength building and try and maintain a healthy calorie dense diet";}
                return bip;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrainingPlan.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
}}
