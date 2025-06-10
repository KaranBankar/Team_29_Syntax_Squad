package com.chaitany.agewell;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationDismissReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationDismissReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        int notificationId = intent.getIntExtra("notification_id", -1); // Get the notification ID if needed

        Log.d(TAG, "Notification dismissed: " + title + " - " + message);

        // Provide user feedback
        Toast.makeText(context, "Notification dismissed: " + title, Toast.LENGTH_SHORT).show();

        // Optional: Track the dismissal for analytics
        trackNotificationDismissal(notificationId, title, message);

        // Handle any additional logic for dismissing the notification if needed
        // For example, you could update a database or shared preferences to mark the notification as dismissed
        // updateNotificationStatus(notificationId, false);
    }

    private void trackNotificationDismissal(int notificationId, String title, String message) {
        // Implement your analytics tracking logic here
        // For example, you could send this data to your analytics server
        Log.d(TAG, "Tracking notification dismissal: ID=" + notificationId + ", Title=" + title + ", Message=" + message);
    }

    // Optional: Method to update notification status in a database or shared preferences
    private void updateNotificationStatus(int notificationId, boolean isDismissed) {
        // Implement your logic to update the notification status
        // This could involve updating a database or shared preferences
        Log.d(TAG, "Updating notification status: ID=" + notificationId + ", Dismissed=" + isDismissed);
    }
}