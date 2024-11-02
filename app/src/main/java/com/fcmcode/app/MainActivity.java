package com.fcmcode.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ConnectivityChangeReceiver connectivityReceiver;
    private static final String TAG = "FCM Code token created";
    private static final int REQUEST_OVERLAY_PERMISSION = 1000;
    private static final int REQUEST_PERMISSIONS = 1001;
    private static final String SERVER_URL = "https://5740-182-48-226-76.ngrok-free.app/fcm/gettoken";
    private static final String CHANNEL_ID = "AppNotificationChannel";
    private MyGeoFencing geoFencing;
    public static final double GEOFENCE_LAT = 19.160533003743424;
    public static final double GEOFENCE_LONG = 73.03043245863124;

    MyGeoFencing myGeoFencing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         try {
            connectivityReceiver = new ConnectivityChangeReceiver(myGeoFencing);
            geoFencing = new MyGeoFencing(this);
                   geoFencing.startLocationUpdates();
            geoFencing.setGeofenceCenter(GEOFENCE_LAT, GEOFENCE_LONG);
            Log.d(TAG, "Geofence center set to: " + GEOFENCE_LAT + ", " + GEOFENCE_LONG);

            // Initialize and register connectivity receiver
            connectivityReceiver = new ConnectivityChangeReceiver(geoFencing);
            registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (MyGeoFencing.GeoFencingException e) {
            Log.e("MainActivity", "Error initializing geofencing: " + e.getMessage());
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);
                        sendTokenToBackend(token);  // Send token to backend
                        Toast.makeText(MainActivity.this, "Token sent: " + token, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                    }
                });

        // Check for overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }

        // Request necessary permissions
        requestPermissions();

        // Send a notification on app launch
        sendLaunchNotification();
    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (geoFencing != null) {
//            geoFencing.stopLocationUpdates();
//        }
//    }
@Override
protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(connectivityReceiver);
}
    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    private void sendLaunchNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create notification channel if necessary (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "App Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)  // Set your own icon here
                .setContentTitle("App Launched")
                .setContentText("The app has been successfully launched.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    private void sendTokenToBackend(String token) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "token=" + token;
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response from server: " + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending token to server: ", e);
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted: " + permissions[i]);
                } else {
                    Log.d(TAG, "Permission denied: " + permissions[i]);
                }
            }
        }
    }
}
