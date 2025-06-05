package com.example.myapplication3;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
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

public class SettingsActivity extends AppCompatActivity {

    private TextView currentEmailText, currentHeightText, currentUsernameText;
    private Button changeEmailBtn, changePasswordBtn, changeHeightBtn, signOutBtn, changeUsernameBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        // Initialize views
        initViews();

        // Load current user data
        loadUserData();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        currentEmailText = findViewById(R.id.currentEmailText);
        currentHeightText = findViewById(R.id.currentHeightText);
        changeEmailBtn = findViewById(R.id.changeEmailBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        changeHeightBtn = findViewById(R.id.changeHeightBtn);
        signOutBtn = findViewById(R.id.signOutBtn);
        currentUsernameText = findViewById(R.id.currentUsernameText);
        changeUsernameBtn = findViewById(R.id.changeUsernameBtn);
    }

    private void loadUserData() {
        // Display current email using FirebaseHandler
        String currentEmail = FirebaseHandler.getCurrentUserEmail();
        if (currentEmail != null) {
            currentEmailText.setText("Current Email: " + currentEmail);
        }

        // Load user data from Firebase using FirebaseHandler
        FirebaseHandler.loadUserData(new FirebaseHandler.UserDataCallback() {
            @Override
            public void onUserDataReceived(FirebaseHandler.User userData) {
                runOnUiThread(() -> {
                    currentHeightText.setText("Current Height: " + userData.height + " cm");
                    currentUsernameText.setText("Current Username: " +
                            (userData.username != null ? userData.username : "Not set"));
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Error loading user data: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setClickListeners() {
        changeEmailBtn.setOnClickListener(v -> showChangeEmailDialog());
        changePasswordBtn.setOnClickListener(v -> showChangePasswordDialog());
        changeHeightBtn.setOnClickListener(v -> showChangeHeightDialog());
        signOutBtn.setOnClickListener(v -> signOut());
        changeUsernameBtn.setOnClickListener(v -> showChangeUsernameDialog());
    }

    private void showChangeEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Email");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        final EditText newEmailInput = new EditText(this);
        newEmailInput.setHint("New Email");
        newEmailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(newEmailInput);

        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newEmail = newEmailInput.getText().toString().trim();

            if (currentPassword.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use FirebaseHandler to change email
            FirebaseHandler.changeEmail(currentPassword, newEmail, new FirebaseHandler.EmailUpdateCallback() {
                @Override
                public void onSuccess(String newEmail) {
                    runOnUiThread(() -> {
                        currentEmailText.setText("Current Email: " + newEmail);
                        Toast.makeText(SettingsActivity.this, "Email updated successfully!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangeUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Username");

        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("Enter new username");
        usernameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        usernameInput.setPadding(50, 40, 50, 10);

        builder.setView(usernameInput);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newUsername = usernameInput.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newUsername.length() < 3 || newUsername.length() > 20) {
                Toast.makeText(this, "Username must be between 3-20 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use FirebaseHandler to update username
            FirebaseHandler.updateUsername(newUsername, new FirebaseHandler.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        currentUsernameText.setText("Current Username: " + newUsername);
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        final EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use FirebaseHandler to change password
            FirebaseHandler.changePassword(currentPassword, newPassword, new FirebaseHandler.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangeHeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Height");

        final EditText heightInput = new EditText(this);
        heightInput.setHint("Enter your height in cm");
        heightInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        heightInput.setPadding(50, 40, 50, 10);

        builder.setView(heightInput);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String heightStr = heightInput.getText().toString().trim();
            if (heightStr.isEmpty()) {
                Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int height = Integer.parseInt(heightStr);
                if (height < 100 || height > 250) {
                    Toast.makeText(this, "Please enter a valid height (100-250 cm)", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Use FirebaseHandler to update height
                FirebaseHandler.updateHeight(height);
                currentHeightText.setText("Current Height: " + height + " cm");

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Use FirebaseHandler to sign out
            FirebaseHandler.signOut();
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        builder.setNegativeButton("No", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to this activity
        loadUserData();
    }
}