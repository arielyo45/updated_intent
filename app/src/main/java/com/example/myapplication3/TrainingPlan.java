package com.example.myapplication3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication3.R;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Result;



public class TrainingPlan extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private SharedPreferences sharedPreferences;

    // Data Model inside the Activity
    public static class WorkoutDay {
        String day;
        String workout;

        public WorkoutDay(String day, String workout) {
            this.day = day;
            this.workout = workout;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_plan);

        sharedPreferences = getSharedPreferences("WorkoutPrefs", MODE_PRIVATE);
        List<WorkoutDay> days = Arrays.asList(
                new WorkoutDay("Sunday", sharedPreferences.getString("Sunday", "")),
                new WorkoutDay("Monday", sharedPreferences.getString("Monday", "")),
                new WorkoutDay("Tuesday", sharedPreferences.getString("Tuesday", "")),
                new WorkoutDay("Wednesday", sharedPreferences.getString("Wednesday", "")),
                new WorkoutDay("Thursday", sharedPreferences.getString("Thursday", "")),
                new WorkoutDay("Friday", sharedPreferences.getString("Friday", "")),
                new WorkoutDay("Saturday", sharedPreferences.getString("Saturday", ""))
        );

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutAdapter(days, () -> {
            saveData(days);
            checkForRestDaySuggestion(days);
        });
        recyclerView.setAdapter(adapter);

        scheduleWeeklyReset();
    }

    private void saveData(List<WorkoutDay> days) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (WorkoutDay day : days) {
            editor.putString(day.day, day.workout);
        }
        editor.apply();
    }

    private void checkForRestDaySuggestion(List<WorkoutDay> days) {
        boolean allFilled = true;
        for (WorkoutDay day  : days) {
            if (day.workout.isEmpty()) {
                allFilled = false;
                break;
            }
        }
        if (allFilled) {
            new AlertDialog.Builder(this)
                    .setTitle("Rest Day Suggestion")
                    .setMessage("You've scheduled workouts every day! Consider adding rest days.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void scheduleWeeklyReset() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(ResetWork.class, 7, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("resetWork", ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}

// RecyclerView Adapter
class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
    private final List<TrainingPlan.WorkoutDay> days;
    private final Runnable onDataChanged;

    public WorkoutAdapter(List<TrainingPlan.WorkoutDay> days, Runnable onDataChanged) {
        this.days = days;
        this.onDataChanged = onDataChanged;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        EditText workoutInput;

        ViewHolder(View view) {
            super(view);
            dayText = view.findViewById(R.id.titleText);
            workoutInput = view.findViewById(R.id.workoutInput);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrainingPlan.WorkoutDay item = days.get(position);
        holder.dayText.setText(item.day);
        holder.workoutInput.setText(item.workout);

        holder.workoutInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                item.workout = s.toString();
                onDataChanged.run();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}

