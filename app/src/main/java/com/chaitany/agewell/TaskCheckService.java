package com.chaitany.agewell;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import androidx.annotation.Nullable;
import java.util.Calendar;

public class TaskCheckService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Dashboard.triggerTimeBasedCheck(this);
        stopSelf();
        performTimeCheck();
        return START_NOT_STICKY;
    }

    private void performTimeCheck() {
        // Get application context
        Context context = getApplicationContext();

        // Get current hour
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        // Access SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("time_blocks", MODE_PRIVATE);

        // Check morning tasks (after 12 PM)
        if (hour >= 12 && !prefs.getBoolean("morning_done", false)) {
            NotificationHelper.showNotification(context, "Complete your morning tasks!");
        }

        // Check afternoon tasks (after 6 PM)
        if (hour >= 18 && !prefs.getBoolean("afternoon_done", false)) {
            NotificationHelper.showNotification(context, "Complete your afternoon tasks!");
        }

        // Check night tasks (after 9 PM)
        if (hour >= 21 && !prefs.getBoolean("night_done", false)) {
            NotificationHelper.showNotification(context, "Complete your night tasks!");
        }

        // Stop the service when done
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}