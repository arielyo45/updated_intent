package com.example.myapplication3;


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
    private boolean flag = false;
    private boolean gender;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_login);

        EditText usernameEditText = findViewById(R.id.editTextUsername);
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
                username = usernameEditText.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(FirstTimeLogin.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
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


                if(workouts<10&&workouts>=0){
                if (weight < 140 && weight > 40) {
                    if (height > 120 && height < 230) {

                            flag =true;
                            Toast.makeText(
                                FirstTimeLogin.this,
                                "Weight: " + weight + " kg\nHeight: " + height + " cm\nWorkouts/Week: " + workouts + "\nGender: " + gender + "\nUsername: " + username,
                                Toast.LENGTH_LONG
                            ).show();
                            FirebaseHandler.saveFirstTimeUser(height, workouts, weight, gender, username);

                    }

                    }

                }
                if (!flag)
                {
                    Toast.makeText(
                            FirstTimeLogin.this,
                            "please use correct information",
                            Toast.LENGTH_LONG
                    ).show();
                }

            }
        });
    }
}








