package com.chaitany.agewell;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VolumeButtonService extends Service {
    private static final String CHANNEL_ID = "VolumeButtonServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final int TRIPLE_CLICK_THRESHOLD = 1000; // 1 second window for triple click
    private long lastVolumeUpPress = 0;
    private int volumeUpCount = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseRef;
    private String userMobile;

    private final BroadcastReceiver volumeButtonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                long currentTime = System.currentTimeMillis();
                // Check if volume up was pressed (you may need to adjust logic based on testing)
                if (currentTime - lastVolumeUpPress < TRIPLE_CLICK_THRESHOLD) {
                    volumeUpCount++;
                    if (volumeUpCount >= 3) {
                        // Triple click detected, share location
                        shareLocation();
                        volumeUpCount = 0; // Reset counter
                    }
                } else {
                    volumeUpCount = 1; // Reset to 1 for a new sequence
                }
                lastVolumeUpPress = currentTime;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize Firebase reference
        SharedPreferences prefs = getSharedPreferences("UserLogin", MODE_PRIVATE);
        userMobile = prefs.getString("mobile", null);
        if (userMobile != null) {
            databaseRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userMobile)
                    .child("emergency_contacts");
        }
        // Register volume button receiver
        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeButtonReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY; // Service restarts if killed
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Volume Button Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, EmergencyContact.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Emergency Location Service")
                .setContentText("Listening for volume button triple-click to share location")
                .setSmallIcon(R.drawable.app_logo) // Replace with your app's icon
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void shareLocation() {
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            showToast("Location or SMS permission not granted");
            return;
        }

        // Get location
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
                    sendLocationToContacts(latitude, longitude);
                } else {
                    showToast("Failed to get location");
                }
            }
        });
    }

    private void sendLocationToContacts(String latitude, String longitude) {
        if (databaseRef == null) {
            showToast("Database reference not initialized");
            return;
        }

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showToast("No emergency contacts found!");
                    return;
                }

                SmsManager smsManager = SmsManager.getDefault();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String phoneNumber = contactSnapshot.child("phone").getValue(String.class);
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        String message = "Emergency! My current location: " +
                                "https://www.google.com/maps?q=" + latitude + "," + longitude;
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    }
                }
                showToast("Location sent to emergency contacts");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to retrieve contacts: " + error.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(volumeButtonReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}