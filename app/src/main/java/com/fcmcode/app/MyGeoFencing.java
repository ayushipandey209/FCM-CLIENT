package com.fcmcode.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyGeoFencing {
    private static final String TAG = "MyGeoFencing";
    public static final float GEOFENCE_RADIUS = 100;
    private static final long UPDATE_INTERVAL = 60000;
    private static final long FASTEST_INTERVAL = 30000;
    private static final String SERVER_URL = "https://5740-182-48-226-76.ngrok-free.app/fcm/sendalertGF";
    private static final int MAX_RETRIES = 3;

    private final Context context;
    private final ExecutorService executorService;
    private LocationCallback locationCallback;
    private double centerLatitude;
    private double centerLongitude;
    private boolean isInsideGeofence = true;
    private int retryCount = 0;
    private boolean isMonitoring = false;
    public double currentLat;
    public double currentLng;
    public static class GeoFencingException extends Exception {
        public GeoFencingException(String message) {
            super(message);
        }

        public GeoFencingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public MyGeoFencing(Context context) throws GeoFencingException {
        if (context == null) {
            throw new GeoFencingException("Context cannot be null");
        }
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        setupLocationCallback();
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    public void setGeofenceCenter(double latitude, double longitude) throws GeoFencingException {
        validateCoordinates(latitude, longitude);
        this.centerLatitude = latitude;
        this.centerLongitude = longitude;
        Log.d(TAG, "Geofence center set to: " + latitude + ", " + longitude);
    }

    private void validateCoordinates(double latitude, double longitude) throws GeoFencingException {
        if (latitude < -90 || latitude > 90) {
            throw new GeoFencingException("Invalid latitude value: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new GeoFencingException("Invalid longitude value: " + longitude);
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(TAG, "Location result is null");
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    checkGeofence(location);
                }
            }
        };
    }

    public void startLocationUpdates() throws GeoFencingException {
        if (!isMonitoring) {
            try {
                checkLocationPermission();
                LocationRequest locationRequest = createLocationRequest();

                LocationServices.getFusedLocationProviderClient(context)
                        .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                        .addOnSuccessListener(aVoid -> {
                            isMonitoring = true;
                            Log.d(TAG, "Location updates started successfully");

                            getCurrentLocation();
                        })
                        .addOnFailureListener(e -> {
                            isMonitoring = false;
                            Log.e(TAG, "Failed to start location updates: " + e.getMessage());
                        });

            } catch (Exception e) {
                throw new GeoFencingException("Failed to start location updates", e);
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.getFusedLocationProviderClient(context)
                .getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        checkGeofence(location);
                    }
                });
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .build();
    }

    private void checkLocationPermission() throws GeoFencingException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            throw new GeoFencingException("Location permission not granted");
        }
    }

    private void checkGeofence(Location location) {
        float[] distance = new float[1];
        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                centerLatitude,
                centerLongitude,
                distance
        );

        boolean currentlyInside = distance[0] <= GEOFENCE_RADIUS;

        if (currentlyInside != isInsideGeofence) {
            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
            isInsideGeofence = currentlyInside;
            String message = String.format(
                    "Device %s geofence! Distance: %.2f meters, Location: %.6f, %.6f",
                    currentlyInside ? "entered" : "left",
                    distance[0],
                    location.getLatitude(),
                    location.getLongitude()
            );
            sendAlert(message , currentLat , currentLng);
        }
    }

    public void sendAlert(String alertMessage , double lat , double lng) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                String postData = "message=" + alertMessage;
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

    private void handleSendError(String error, String alertMessage) {
        Log.e(TAG, "Failed to send alert: " + error);
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            // Exponential backoff retry
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "Retrying alert send. Attempt " + retryCount);
                sendAlert(alertMessage , currentLat , currentLng);
            }, 1000 * (long) Math.pow(2, retryCount));
        } else {
            retryCount = 0;
            Log.e(TAG, "Max retries reached for alert: " + alertMessage);
        }
    }

    private String getDeviceId() {
        String deviceId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        return deviceId != null ? deviceId : "unknown_device";
    }

    public void stopLocationUpdates() {
        if (isMonitoring) {
            LocationServices.getFusedLocationProviderClient(context)
                    .removeLocationUpdates(locationCallback)
                    .addOnSuccessListener(aVoid -> {
                        isMonitoring = false;
                        Log.d(TAG, "Location updates stopped successfully");
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Failed to stop location updates: " + e.getMessage())
                    );
        }
    }

    public void cleanup() {
        stopLocationUpdates();
        executorService.shutdown();
    }
}