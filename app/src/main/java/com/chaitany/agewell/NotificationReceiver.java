package com.chaitany.agewell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            int notificationId = intent.getIntExtra("notification_id", -1); // Get the notification ID

            if (title != null && message != null) {
                Log.d(TAG, "Received notification: " + title + " - " + message);
                NotificationUtils.showNotification(context, title, message, notificationId);
                Toast.makeText(context, "Notification received: " + title, Toast.LENGTH_SHORT).show(); // Optional user feedback
            } else {
                Log.e(TAG, "Notification title or message is null");
            }
        } else {
            Log.e(TAG, "Received null intent");
        }
    }
}