package com.fcmcode.app;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MyLockUnlock extends Activity {

    private static final String PREFS_NAME = "LockScreenPrefs";
    private static final String KEY_IS_RUNNING = "isRunning";
    private static final String TAG = "locknunlock";
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLockScreenRunning(true);

        // Set up the full white screen
        FrameLayout layout = new FrameLayout(this);
        layout.setBackgroundColor(Color.WHITE);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        setContentView(layout);

        // Prevent screen interactions
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        // Lock device and start Lock Task Mode
        lockDevice(true);
    }

    private void lockDevice(boolean status) {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.setLockTaskPackages(adminComponent, new String[]{getPackageName()});
            devicePolicyManager.addUserRestriction(adminComponent, UserManager.DISALLOW_CREATE_WINDOWS);

            if (status) {
                Log.d(TAG, "Locking the device");
                startLockTask();
                Toast.makeText(this, "Device Locked", Toast.LENGTH_SHORT).show();
            } else {
                stopLockTask();  // Unlock device after a delay if needed
                Log.d(TAG, "Device unlocked");
                Toast.makeText(getApplicationContext(), "Device Unlocked", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Admin permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Disable volume buttons
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Disable volume buttons
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mark the activity as not running when destroyed
        setLockScreenRunning(false);
        stopLockTask();
    }

    private void setLockScreenRunning(boolean isRunning) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_RUNNING, isRunning);
        editor.apply();
    }

    // SharedPreferences helper method to retrieve the isRunning state
    public static boolean isLockScreenRunning(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_RUNNING, false);
    }

    public void unlockDevice() {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            // Remove restrictions
            devicePolicyManager.clearUserRestriction(adminComponent, UserManager.DISALLOW_CREATE_WINDOWS);

            // Stop Lock Task Mode
            stopLockTask();

            Log.d(TAG, "Unlocking the device");
            Toast.makeText(this, "Device Unlocked", Toast.LENGTH_SHORT).show();

            // End activity
            finish();
        } else {
            Toast.makeText(this, "Admin permission required", Toast.LENGTH_SHORT).show();
        }
    }
}
