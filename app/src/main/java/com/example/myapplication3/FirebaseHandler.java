package com.example.myapplication3;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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
                }
            });
        }
    }
    public void SignUp(String sEmail, String sPassword) {
        if (TextUtils.isEmpty(sEmail) || TextUtils.isEmpty(sPassword)) {
            Toast.makeText(context, "You are missing details", Toast.LENGTH_SHORT).show();
        } else {
            // Create user in Firebase Authentication
            auth.createUserWithEmailAndPassword(sEmail, sPassword)
                    .addOnSuccessListener(authResult -> {
                        // After successful registration, update the Firebase Realtime Database with user info
                        String userId = authResult.getUser().getUid();  // Get the unique ID of the newly created user

                        // Create a map to store user information in the database
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("email", sEmail);  // Store the email
                        userInfo.put("password", sPassword);  // Optional: you can choose to not store password for security reasons

                        // Add user info to Firebase Realtime Database under a "users" node
                        myRef.child("users").child(userId).setValue(userInfo)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Registration Successful and Database Updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to Update Database: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }
    }
}
