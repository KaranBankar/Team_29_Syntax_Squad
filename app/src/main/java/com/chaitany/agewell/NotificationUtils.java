package com.chaitany.agewell;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationUtils {
    private static final String CHANNEL_ID = "APPOINTMENT_CHANNEL";
    private static final String TAG = "NotificationUtils";

    public static void showNotification(Context context, String title, String message, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if the NotificationManager is null
        if (manager == null) {
            Log.e(TAG, "NotificationManager is null. Notification not shown.");
            return;
        }

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Appointment Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for appointment reminders");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000}); // Vibration pattern
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(alarmSound, null); // Set sound for the channel
            manager.createNotificationChannel(channel);
        }

        // Create an intent to open the app when the notification is clicked
        Intent notificationIntent = new Intent(context, ActivityAppointment.class); // Change ActivityAppointment to your target activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 1000, 500, 1000}) // Vibration pattern for the notification
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set sound for the notification
                .setContentIntent(pendingIntent) // Set the intent to open the app
                .addAction(R.drawable.ic_delete, "Dismiss", createDismissIntent(context, title, message, notificationId)); // Add a dismiss action

        // Notify with a unique ID
        manager.notify(notificationId, builder.build());
        Log.d(TAG, "Notification shown: " + title);
    }

    private static PendingIntent createDismissIntent(Context context, String title, String message, int notificationId) {
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        dismissIntent.putExtra("title", title);
        dismissIntent.putExtra("message", message);
        dismissIntent.putExtra("notification_id", notificationId); // Pass the notification ID
        return PendingIntent.getBroadcast(context, notificationId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}