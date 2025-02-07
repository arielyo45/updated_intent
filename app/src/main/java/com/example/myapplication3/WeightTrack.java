package com.example.myapplication3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.AsyncListUtil;

import com.google.firebase.auth.FirebaseAuth;

public class WeightTrack extends AppCompatActivity {
    private Button button;
    private int weight;
    private int radio;
    private Button bt;
    private EditText weight2;
    private RadioButton isBulk;
    private RadioButton isLose;
    private RadioButton isStay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weight_track);
        bt = findViewById(R.id.button2);
        weight2 = findViewById(R.id.textView9);
        isBulk = findViewById(R.id.radioButton3);
        isLose = findViewById(R.id.radioButton2);
        isStay = findViewById(R.id.radioButton);


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateWeightAndShowAlert();
            }
        });
    }
    private void updateWeightAndShowAlert() {


        try {
            weight = Integer.parseInt(weight2.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(WeightTrack.this, "Please enter a valid weight!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(weight2 ==null) {
            String currentUserId = FirebaseAuth.getInstance().getUid();;  // Replace with the actual user id (or use auth.getUid() if available)

            FirebaseHandler.getData(currentUserId, this);

                public void onDataReceived(FirebaseHandler.User user) {
                    // You now have access to the user's data
                    int weight = user.weight;
                    int height = user.height;

                    // For example, calculate BMI (assuming height is in meters and weight in kg)
                    double bmi = weight / ( (height / 100.0) * (height / 100.0) ); // if height is stored in centimeters
                    // If height is already in meters, then simply:
                    // double bmi = weight / (height * height);

                    // Display the BMI and a tip based on your criteria
                    String tip;
                    // You might have radio button criteria in your UI; here we simply show the BMI.
                    tip = "Your BMI is: " + String.format("%.2f", bmi);

                    // For demonstration, show an AlertDialog with the data.
                    new AlertDialog.Builder(WeightTrack.this)  // Replace YourActivity with the actual Activity name.
                            .setTitle("User Data")
                            .setMessage("Weight: " + weight + "\nHeight: " + height + "\n" + tip)
                            .setPositiveButton("OK", null)
                            .show();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(WeightTrack  .this, errorMessage, Toast.LENGTH_SHORT).show();
                }
        }

    }



}
