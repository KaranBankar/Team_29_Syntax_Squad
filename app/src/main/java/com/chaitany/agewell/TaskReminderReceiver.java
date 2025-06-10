package com.chaitany.agewell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Intent serviceIntent = new Intent(context, TaskCheckService.class);
            context.startService(serviceIntent);
        }).start();
    }
}