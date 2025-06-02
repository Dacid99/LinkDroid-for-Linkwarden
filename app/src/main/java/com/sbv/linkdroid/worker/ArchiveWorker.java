package com.sbv.linkdroid.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sbv.linkdroid.database.AppDatabase;

public class ArchiveWorker extends Worker {

    Context context;
    private static final String TAG = "ArchiveWorker";

    public ArchiveWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        AppDatabase db = AppDatabase.get(context);
        db.linkDao().getAll().forEach(it -> {
            Log.i(TAG, "Archive now: "+it.firstName);

            // var result = api.archiveArticle(it.url)

            String result = "";

            if(result != null) {
                new Notifications(context).notifyPendingArchivalDone(it.firstName);
                db.linkDao().delete(it);
            } else {
                // todo, inform user
                Log.e(TAG, "ERROR ARCHIVING");
            }
        });


        return Result.success();
    }
}
