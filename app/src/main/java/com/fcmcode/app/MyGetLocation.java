package com.fcmcode.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MyGetLocation extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private static final String TAG = "SendMessagse";
    private static final String PREDEFINED_PHONE_NUMBER = "9730022471";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location or SMS permissions not granted.");
            stopSelf();
            return START_NOT_STICKY;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    sendSms(location);
                } else {
                    requestLocationUpdates();
                }
            }
        });

        return START_STICKY;
    }



    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000) // 1-second interval
                .setFastestInterval(500) // Fastest update interval
                .setNumUpdates(1); // Request only one update

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    sendSms(location); // Send SMS with location when result is available
                }
            }
        }, getMainLooper());
    }

    private void sendSms(Location location) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = "Password failed more than 1 time. Location: "
                + "Latitude: " + location.getLatitude() + ", "
                + "Longitude: " + location.getLongitude();

        try {
            smsManager.sendTextMessage(PREDEFINED_PHONE_NUMBER, null, message, null, null);
            Log.d(TAG, "SMS with location sent to: " + PREDEFINED_PHONE_NUMBER);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
