package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ResetWork extends Worker {
    public ResetWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        return Result.success();
    }
}
