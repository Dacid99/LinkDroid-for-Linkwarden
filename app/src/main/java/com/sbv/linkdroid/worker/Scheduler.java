package com.sbv.linkdroid.worker;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class Scheduler {

    Context context;

    public Scheduler(Context context) {
        this.context = context;
    }

    public void schedule() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ArchiveWorker.class, 1, TimeUnit.HOURS, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("pendingArchival", ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    public void now() {
        WorkRequest request = new OneTimeWorkRequest.Builder(ArchiveWorker.class).build();
        WorkManager.getInstance(context).enqueue(request);
    }

    public void scheduleAndRunNow() {
        now();
        schedule();
    }
}
