package com.sbv.linkdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sbv.linkdroid.worker.Scheduler;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Scheduler(context).schedule();
    }
}
