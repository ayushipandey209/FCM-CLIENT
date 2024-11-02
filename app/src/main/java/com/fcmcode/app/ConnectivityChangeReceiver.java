package com.fcmcode.app;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "MyGeoFencing";
    private MyGeoFencing myGeoFencing;
    private String lastConnectionType = "";
    private static final float GEOFENCE_RADIUS = 5;
    public double currentLat;
    public double currentLng;
    private MyGeoFencing geoFencing;

    public ConnectivityChangeReceiver(MyGeoFencing geoFencing) {
        this.myGeoFencing = geoFencing;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        String currentConnectionType = getConnectionType(activeNetwork);

        // Check if connection type has changed
        if (!currentConnectionType.equals(lastConnectionType)) {
            Log.d(TAG, "Network changed from " + lastConnectionType + " to " + currentConnectionType);
            lastConnectionType = currentConnectionType;

            // Immediately check geofence
            checkGeofenceOnNetworkChange(context, currentConnectionType);
        }
    }

    private String getConnectionType(NetworkInfo activeNetwork) {
        if (activeNetwork == null) {
            return "NO_CONNECTION";
        }
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ? "WIFI" : "MOBILE";
    }

    private void checkGeofenceOnNetworkChange(Context context, String networkType) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                       currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                        if (location != null) {
                            float[] distance = new float[1];
                            Location.distanceBetween(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    myGeoFencing.getCenterLatitude(),
                                    myGeoFencing.getCenterLongitude(),
                                    distance
                            );
                            boolean isInsideGeofence = distance[0] <= GEOFENCE_RADIUS;
                            String connectionMessage = "";
                            switch(networkType) {
                                case "WIFI":
                                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                    String ssid = wifiInfo.getSSID();
                                    connectionMessage = "Switched to WiFi: " + ssid;
                                    break;
                                case "MOBILE":
                                    connectionMessage = "Switched to Mobile Data";
                                    break;
                                case "NO_CONNECTION":
                                    connectionMessage = "Lost network connection";
                                    break;
                            }

                            String alertMessage = connectionMessage +
                                    " - Device is " + (isInsideGeofence ? "inside" : "outside") +
                                    " geofence (Distance: " + String.format("%.2f", distance[0]) + "m)";

                            Log.d(TAG, alertMessage);

                            if (!isInsideGeofence) {
                                String warningMessage = "WARNING: Device has left geofence area during network switch!";
                                myGeoFencing.sendAlert(warningMessage , currentLat , currentLng);
                                Log.d(TAG, warningMessage);
                            }
                        } else {
                            Log.e(TAG, "Could not get current location");
                            myGeoFencing.sendAlert("Network changed but couldn't verify location" , geoFencing.currentLat , geoFencing.currentLng);
                        }
                    }
                });
    }
}