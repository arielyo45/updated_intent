package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class ResetWork extends ListenableWorker  {

    public ResetWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();

        try {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            future.set(Result.success());
        } catch (Exception e) {
            future.setException(e);
        }

        return future;
    }
}
