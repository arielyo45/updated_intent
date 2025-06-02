package com.example.myapplication3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class FriendAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> Friends;
    private LayoutInflater inflater;
    private DatabaseReference usersRef;

    public FriendAdapter(Context context, List<Friend> buddies) {
        this.context = context;
        this.Friends = buddies;
        this.inflater = LayoutInflater.from(context);
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public int getCount() {
        return Friends.size();
    }

    @Override
    public Object getItem(int position) {
        return Friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_friend, parent, false);
            holder = new ViewHolder();
            holder.nameText = convertView.findViewById(R.id.buddy_name);
            holder.workoutText = convertView.findViewById(R.id.buddy_workout);
            holder.frequencyText = convertView.findViewById(R.id.buddy_frequency);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Friend buddy = Friends.get(position);

        holder.nameText.setText(buddy.getDisplayName());
        holder.workoutText.setText("Workout: " + buddy.getWorkout());
        holder.frequencyText.setText("Weekly workouts: " + buddy.getWorkoutFrequency());

        // Set click listener for the entire item
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserContactInfo(buddy);
            }
        });

        return convertView;
    }

    private void showUserContactInfo(Friend friend) {
        // Get the email from Firebase Auth for this user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to view contact information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Since we can't directly get another user's email from Firebase Auth,
        // we'll need to get it from the user's profile data if stored there
        // or use a different approach

        usersRef.child(friend.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.child("email").getValue(String.class);
                    String username = friend.getUsername();
                    String workout = friend.getWorkout();
                    String day = friend.getDay();

                    showContactDialog(username, email, workout, day);
                } else {
                    Toast.makeText(context, "User information not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error retrieving contact info: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showContactDialog(String username, String email, String workout, String day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Contact Information");

        String message = "Username: " + username + "\n\n";

        if (email != null && !email.isEmpty()) {
            message += "Email: " + email + "\n\n";
        } else {
            message += "Email: Not available\n\n";
        }

        message += "Workout: " + workout + "\n";
        message += "Day: " + day;

        builder.setMessage(message);

        builder.setPositiveButton("OK", null);

        // Optional: Add a button to copy email to clipboard
        if (email != null && !email.isEmpty()) {
            builder.setNeutralButton("Copy Email", (dialog, which) -> {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Email", email);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show();
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class ViewHolder {
        TextView nameText;
        TextView workoutText;
        TextView frequencyText;
    }
}