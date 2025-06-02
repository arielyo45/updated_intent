package com.example.myapplication3;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindFriends extends AppCompatActivity {

    private Spinner daySpinner;
    private ListView buddiesListView;
    private TextView noBuddiesText;
    private FriendAdapter adapter;
    private List<Friend> buddiesList;

    private FirebaseUser currentUser;
    private DatabaseReference trainingPlansRef;
    private DatabaseReference usersRef;

    private String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        initializeViews();
        setupFirebase();
        setupSpinner();
        setupListView();
    }

    private void initializeViews() {
        daySpinner = findViewById(R.id.day_spinner);
        buddiesListView = findViewById(R.id.buddies_list_view);
        noBuddiesText = findViewById(R.id.no_buddies_text);

        buddiesList = new ArrayList<>();
    }

    private void setupFirebase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        trainingPlansRef = FirebaseDatabase.getInstance().getReference("TrainingPlans");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                daysOfWeek
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(spinnerAdapter);

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDay = daysOfWeek[position];
                findWorkoutBuddies(selectedDay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListView() {
        adapter = new FriendAdapter(this, buddiesList);
        buddiesListView.setAdapter(adapter);

        // Add click listener to handle item clicks
        buddiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend selectedFriend = buddiesList.get(position);
                showContactDialog(selectedFriend);
            }
        });
    }

    private void showContactDialog(Friend friend) {
        String message = "Contact Information:\n\n" +
                "Name: " + friend.getDisplayName() + "\n" +
                "Email: " + (friend.getEmail() != null ? friend.getEmail() : "Not available") + "\n" +
                "Workout: " + friend.getWorkout() + "\n" +
                "Weekly Frequency: " + friend.getWorkoutFrequency() + " times";

        new AlertDialog.Builder(this)
                .setTitle("Workout Buddy Contact")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void findWorkoutBuddies(String selectedDay) {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to find workout buddies", Toast.LENGTH_SHORT).show();
            return;
        }

        buddiesList.clear();
        adapter.notifyDataSetChanged();

        // First, get current user's workout for the selected day
        trainingPlansRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot currentUserSnapshot) {
                if (!currentUserSnapshot.exists()) {
                    showNoBuddiesMessage("You don't have any workouts planned for " + selectedDay);
                    return;
                }

                String currentUserWorkout = "";
                if (currentUserSnapshot.hasChild(selectedDay)) {
                    currentUserWorkout = currentUserSnapshot.child(selectedDay).getValue(String.class);
                }

                if (currentUserWorkout == null || currentUserWorkout.trim().isEmpty()) {
                    showNoBuddiesMessage("You don't have any workouts planned for " + selectedDay);
                    return;
                }

                // Now search for other users with similar workouts
                searchForSimilarWorkouts(selectedDay, currentUserWorkout.trim().toLowerCase());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindFriends.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchForSimilarWorkouts(String day, String currentUserWorkout) {
        trainingPlansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> potentialBuddyIds = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    // Skip current user
                    if (userId.equals(currentUser.getUid())) {
                        continue;
                    }

                    if (userSnapshot.hasChild(day)) {
                        String otherUserWorkout = userSnapshot.child(day).getValue(String.class);
                        if (otherUserWorkout != null && !otherUserWorkout.trim().isEmpty()) {
                            // Check for similar workouts
                            if (isWorkoutSimilar(currentUserWorkout, otherUserWorkout.trim().toLowerCase())) {
                                potentialBuddyIds.add(userId);
                            }
                        }
                    }
                }

                if (potentialBuddyIds.isEmpty()) {
                    showNoBuddiesMessage("No workout buddies found for " + day);
                } else {
                    fetchUserDetails(potentialBuddyIds, day);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindFriends.this, "Error searching for buddies: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWorkoutSimilar(String workout1, String workout2) {
        // Simple similarity check - you can make this more sophisticated
        String[] keywords1 = workout1.split("\\s+");
        String[] keywords2 = workout2.split("\\s+");

        // Check for common workout keywords
        List<String> commonTerms = Arrays.asList(
                "chest", "back", "legs", "arms", "shoulders", "cardio", "running", "cycling",
                "push", "pull", "squat", "deadlift", "bench", "yoga", "pilates", "hiit",
                "strength", "weight", "gym", "fitness", "abs", "core"
        );

        for (String term : commonTerms) {
            if (workout1.contains(term) && workout2.contains(term)) {
                return true;
            }
        }

        // Also check for exact word matches
        for (String word1 : keywords1) {
            for (String word2 : keywords2) {
                if (word1.length() > 3 && word1.equals(word2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void fetchUserDetails(List<String> userIds, String day) {
        for (String userId : userIds) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    if (userSnapshot.exists()) {
                        // Get user details
                        String gender = userSnapshot.child("gender").getValue(String.class);
                        String username = userSnapshot.child("username").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);
                        Integer workoutFreq = userSnapshot.child("workoutFrequency").getValue(Integer.class);

                        // Get workout details
                        trainingPlansRef.child(userId).child(day).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot workoutSnapshot) {
                                String workout = workoutSnapshot.getValue(String.class);

                                Friend friend = new Friend(
                                        userId,
                                        gender != null ? gender : "Unknown",
                                        workoutFreq != null ? workoutFreq : 0,
                                        workout != null ? workout : "",
                                        day,
                                        username != null ? username : "Anonymous",
                                        email != null ? email : "Not available"
                                );

                                buddiesList.add(friend);
                                adapter.notifyDataSetChanged();
                                hideBuddiesMessage();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void showNoBuddiesMessage(String message) {
        noBuddiesText.setText(message);
        noBuddiesText.setVisibility(View.VISIBLE);
        buddiesListView.setVisibility(View.GONE);
    }

    private void hideBuddiesMessage() {
        noBuddiesText.setVisibility(View.GONE);
        buddiesListView.setVisibility(View.VISIBLE);
    }
}