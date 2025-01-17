package com.example.myapplication3;

import static com.google.firebase.database.core.operation.OperationSource.Source.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

public class FirstTimeLogin extends AppCompatActivity {
private int height;
private int weight;
private int workouts;
private boolean gender;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_login);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        EditText weightEditText = findViewById(R.id.editTextText3);
        EditText heightEditText = findViewById(R.id.editTextText4);
        EditText workoutEditText = findViewById(R.id.editTextText2);
        RadioButton isMale = findViewById(R.id.radio_male);
        RadioButton isFemale = findViewById(R.id.radio_female);
        Button submitButton = findViewById(R.id.button5);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    weight = Integer.parseInt(weightEditText.getText().toString().trim());
                    height = Integer.parseInt(heightEditText.getText().toString().trim());
                    workouts = Integer.parseInt(workoutEditText.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(FirstTimeLogin.this, "Please enter valid numbers!", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (isMale.isChecked()) {
                    gender = true;
                } else if (isFemale.isChecked()) {
                    gender = false;
                } else {
                    Toast.makeText(FirstTimeLogin.this, "Please select your gender!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String sex = gender  ? "Male" : "Female";
                Toast.makeText(
                        FirstTimeLogin.this,
                        "Weight: " + weight + " kg\nHeight: " + height + " cm\nWorkouts/Week: " + workouts + "\nGender: " + gender,
                        Toast.LENGTH_LONG
                ).show();
            }
            User user = new User(weight, height, workouts, isMale);

            // Push the data to Firebase Database
                mDatabase.child("users").push().setValue(user)
                        .addOnSuccessListener(aVoid -> {
                Toast.makeText(FirstTimeLogin.this, "Data saved to Firebase!", Toast.LENGTH_SHORT).show();
            })
                    .addOnFailureListener(e -> {
                Toast.makeText(FirstTimeLogin.this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });


        });


    }

    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), TheHub.class);
        startActivity(intent);}
    public static class User {
        public int weight;
        public int height;
        public int workoutFrequency;
        public String gender;

        public User(int weight, int height, int workoutFrequency, boolean gender) {
            this.weight = weight;
            this.height = height;
            this.workoutFrequency = workoutFrequency;
            this.gender = gender;
        }
}
