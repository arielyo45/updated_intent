package com.example.myapplication3;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

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
            Toast.makeText(context, "you are acoustic", Toast.LENGTH_SHORT).show();
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
        auth.createUserWithEmailAndPassword(sEmail, sPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "success! ", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "failed ", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }
    public static void saveFirstTimeUser(int height, int workouts, int weight, boolean gender) {
        String userId = mDatabase.child("users").push().getKey(); // Generate unique user ID
        if (userId == null) {
            Toast.makeText(context, "Error: Unable to generate user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(weight, height, workouts, gender);
        mDatabase.child("users").child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, TheHub.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    public static class User {
        public int weight;
        public int height;
        public int workoutFrequency;
        public String gender;

        public User(int weight, int height, int workoutFrequency, boolean gender) {
            this.weight = weight;
            this.height = height;
            this.workoutFrequency = workoutFrequency;
            if (gender) this.gender = "Male";
            else this.gender = "Female";
        }
}}
