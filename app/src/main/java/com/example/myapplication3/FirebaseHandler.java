package com.example.myapplication3;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHandler {
    private DatabaseReference databaseReference;
    private static FirebaseAuth auth;
    private int count = 0;
    private static Context context;
    private static final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public interface UserDataCallback {
        void onUserDataReceived(User userData);
        void onError(String error);
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface EmailUpdateCallback {
        void onSuccess(String newEmail);
        void onFailure(String error);
    }

    public FirebaseHandler(FirebaseAuth auth, Context context) {
        FirebaseHandler.auth = auth;
        FirebaseHandler.context = context;
    }

    public FirebaseHandler() {
        databaseReference = FirebaseDatabase.getInstance().getReference("TrainingPlans");
    }

    public void SignIn(String sEmail, String sPassword) {
        if (sEmail.isEmpty() || sPassword.isEmpty()) {
            Toast.makeText(context, "type in the mail and password", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(sEmail, sPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(context, "You are Signed in", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, "Succesfully Signed Up!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, FirstTimeLogin.class);
                            context.startActivity(intent);

                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(context, "Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public static void saveFirstTimeUser(int height, int workouts, int weight, boolean gender, String username, String email) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        String userId = user.getUid();

        User account = new User(weight, height, workouts, gender, username, email);
        mDatabase.child("users").child(userId).setValue(account)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, TheHub.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static void updateWeight(int newWeight) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        mDatabase.child("users").child(userId).child("weight").setValue(newWeight)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Weight updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to update weight: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static void updateHeight(int height) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        mDatabase.child("users").child(userId).child("height").setValue(height)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Height updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to update height: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // New method to update username
    public static void updateUsername(String username, AuthCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = user.getUid();
        mDatabase.child("users").child(userId).child("username").setValue(username)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Username updated successfully!");
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Failed to update username: " + e.getMessage());
                });
    }

    // New method to load user data
    public static void loadUserData(UserDataCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User userData = snapshot.getValue(User.class);
                    if (userData != null) {
                        callback.onUserDataReceived(userData);
                    } else {
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    callback.onError("User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Database error: " + error.getMessage());
            }
        });
    }

    // New method to change email
    public static void changeEmail(String currentPassword, String newEmail, EmailUpdateCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update email
                currentUser.updateEmail(newEmail).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onSuccess(newEmail);
                    } else {
                        String errorMessage = updateTask.getException() != null ?
                                updateTask.getException().getMessage() : "Unknown error";
                        callback.onFailure("Failed to update email: " + errorMessage);
                    }
                });
            } else {
                callback.onFailure("Authentication failed. Check your current password.");
            }
        });
    }

    // New method to change password
    public static void changePassword(String currentPassword, String newPassword, AuthCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update password
                currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onSuccess("Password updated successfully!");
                    } else {
                        String errorMessage = updateTask.getException() != null ?
                                updateTask.getException().getMessage() : "Unknown error";
                        callback.onFailure("Failed to update password: " + errorMessage);
                    }
                });
            } else {
                callback.onFailure("Authentication failed. Check your current password.");
            }
        });
    }

    // New method to sign out
    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    // New method to get current user email
    public static String getCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public void updateWorkoutFrequency() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("TrainingPlans");
        DatabaseReference userWorkoutsRef = databaseReference.child(userId);

        userWorkoutsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int workoutCount = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String workoutValue = child.getValue(String.class);
                        if (workoutValue != null &&
                                !workoutValue.trim().isEmpty() &&
                                !workoutValue.trim().equals("Rest Day")) {
                            workoutCount++;
                        }
                    }
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId);

                    int finalWorkoutCount = workoutCount;
                    userRef.child("workoutFrequency").setValue(workoutCount)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firebase", "Workout frequency updated: " + finalWorkoutCount);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase", "Failed to update workout frequency", e);
                            });

                } else {
                    Log.d("Firebase", "No workouts found for user: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    public void saveTrainingPlan(String userId, Map<String, String> workoutData) {
        databaseReference.child(userId).setValue(workoutData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Training plan saved successfully for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to save training plan for user: " + userId, e);
                });
    }

    public void getTrainingPlan(String userId, FirebaseDataCallback callback) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, String> workouts = new HashMap<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        workouts.put(child.getKey(), child.getValue(String.class));
                    }
                    callback.onDataReceived(workouts);
                } else {
                    callback.onDataReceived(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public interface FirebaseDataCallback {
        void onDataReceived(Map<String, String> workouts);
        void onError(Exception e);
    }

    public static class User {
        public int weight;
        public int height;
        public int workoutFrequency;
        public String gender;
        public String username;
        public String email;

        public User() {}

        public User(int weight, int height, int workoutFrequency, boolean gender, String username, String email) {
            this.weight = weight;
            this.height = height;
            this.workoutFrequency = workoutFrequency;
            this.username = username;
            this.email = email;
            if (gender) this.gender = "Male";
            else this.gender = "Female";
        }
    }
}