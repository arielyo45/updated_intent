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
import android.widget.Button;
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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Result;



public class TrainingPlan extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private SharedPreferences sharedPreferences;
    private boolean alertShown = false;

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
        checkForWeeklyReset();

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

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            for (WorkoutDay day : days) {
                day.workout = "";
            }
            adapter.notifyDataSetChanged();
            saveData(days);
        });
    }

    private void saveData(List<WorkoutDay> days) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (WorkoutDay day : days) {
            editor.putString(day.day, day.workout);
        }
        editor.apply();
    }

    private void checkForRestDaySuggestion(List<WorkoutDay> days) {
        if (alertShown) return;

        boolean allFilled = true;
        for (WorkoutDay day : days) {
            if (day.workout.isEmpty()) {
                allFilled = false;
                break;
            }
        }
        if (allFilled) {
            alertShown = true;
            new AlertDialog.Builder(this)
                    .setTitle("Rest Day Suggestion")
                    .setMessage("You've scheduled workouts every day! Consider adding rest days.")
                    .setPositiveButton("OK", (dialog, which) -> alertShown = false)
                    .show();
        }
    }

    private void checkForWeeklyReset() {
        long lastReset = sharedPreferences.getLong("lastReset", 0);
        long currentTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastReset);
        int lastWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        calendar.setTimeInMillis(currentTime);
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        if (lastWeek != currentWeek) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.putLong("lastReset", currentTime);
            editor.apply();
        }
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
        TextWatcher textWatcher;

        ViewHolder(View view) {
            super(view);
            dayText = view.findViewById(R.id.dayText);
            workoutInput = view.findViewById(R.id.workoutInput);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrainingPlan.WorkoutDay item = days.get(position);
        holder.dayText.setText(item.day);
        holder.workoutInput.setText(item.workout);

        // Remove previous TextWatcher to prevent multiple listeners
        if (holder.textWatcher != null) {
            holder.workoutInput.removeTextChangedListener(holder.textWatcher);
        }

        holder.textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                item.workout = s.toString();
                onDataChanged.run();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        holder.workoutInput.addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}

