package com.example.myapplication3;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHandler {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference myRef = database.getReference();
    private static FirebaseAuth auth;
    private static Context context;
    private static DatabaseReference   mDatabase = FirebaseDatabase.getInstance().getReference();;


    public FirebaseHandler(FirebaseAuth auth,Context context )  {
        FirebaseHandler.auth =auth;
        FirebaseHandler.context = context;

    }
    public void SignIn(String sEmail, String sPassword){
        if (TextUtils.isEmpty(sEmail) || TextUtils.isEmpty(sPassword)) {
            Toast.makeText(context, "type in the mail and password", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(sEmail, sPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(context, "you are genius+", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, TheHub.class);
                        context.startActivity(intent);
                }
            });
        }
    }
    public void SignUp(String sEmail, String sPassword) {
        if (sEmail.isEmpty() || sPassword.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(sEmail, sPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, FirstTimeLogin.class);
                            context.startActivity(intent);

                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(context, "Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("FirebaseAuth", "SignUp Error: " + errorMessage);
                        }
                    }
                });
    }
    public static void saveFirstTimeUser(int height, int workouts, int weight, boolean gender) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userId = user.getUid();
        if (userId == null) {
            Toast.makeText(context, "Error: Unable to generate user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        User account = new User(weight, height, workouts, gender);
        mDatabase.child("users").child(userId).setValue(account)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, TheHub.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    public static void updateWeight(WeightTrack weightTrack, int newWeight) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        mDatabase.child("users").child(userId).child("weight").setValue(newWeight)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Weight updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to update weight: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static void getData(String userId, final Context context) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Convert the snapshot into a User object
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        int weight = user.weight;
                        int height = user.height;
                        double heightInMeters = height / 100.0;
                        double bmi = weight / (heightInMeters * heightInMeters);

                        String tip;
                        if (bmi < 18.5) {
                            tip = "You might consider bulking up.";
                        } else if (bmi < 25) {
                            tip = "You're at a healthy weight.";
                        } else {
                            tip = "You might consider losing some weight.";
                        }

                        String message = "Weight: " + weight +
                                "\nHeight: " + height +
                                "\nBMI: " + String.format("%.2f", bmi) +
                                "\nTip: " + tip;

                        new AlertDialog.Builder(context)
                                .setTitle("User Data")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        Toast.makeText(context, "User data is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
            public static class User {
        public int weight;
        public int height;
        public int workoutFrequency;
        public String gender;
                public User() {
                }

        public User(int weight, int height, int workoutFrequency, boolean gender) {
            this.weight = weight;
            this.height = height;
            this.workoutFrequency = workoutFrequency;
            if (gender) this.gender = "Male";
            else this.gender = "Female";
        }
}}

