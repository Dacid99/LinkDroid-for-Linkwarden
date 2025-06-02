package com.sbv.linkdroid.worker;

import android.content.Context;

public class Notifications {

    Context context;

    private static String NOTIFICATION_CHANNEL_ID = "async_archive_notification_channel";
    private static int NOTIFICATION_ID = 5691;

    public Notifications(Context context) {
        this.context = context;
    }


    public void notifyPendingArchivalDone(String title) {
        // todo
    }


    private void createNotificationChannel() {
        // todo
    }
}
