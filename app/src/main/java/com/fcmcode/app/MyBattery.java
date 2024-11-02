package com.fcmcode.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyBattery {
    private static final String SERVER_URL = "https://57c1-182-48-231-95.ngrok-free.app/fcm/savebattery";
    private static final String TAG = "MyBattery";
    private Context context;

    public MyBattery(Context context) {
        this.context = context;
    }

    public void getBatteryPercentageAndSend() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int)((level * 100.0) / scale);
        sendBatteryToServer(batteryPct);
    }

    private void sendBatteryToServer(int battery) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "battery=" + battery;
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response from server: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Battery level " + battery + "% successfully sent to server");
                } else {
                    Log.e(TAG, "Server returned code: " + responseCode);
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending battery level to server: ", e);
            }
        }).start();
    }
}