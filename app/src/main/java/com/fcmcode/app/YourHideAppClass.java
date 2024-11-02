package com.fcmcode.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.fcmcode.app.MainActivity;
import com.fcmcode.app.R;

public class YourHideAppClass extends Activity {

    final String TAG = "hideunhideapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Entering hide app functionality");
        hideAppIcon();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                UnhideAppIcon();
//            }
//        }, 20000); // 20 seconds delay
    }

    private void hideAppIcon() {
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        getPackageManager().setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        finish();
    }

    public void UnhideAppIcon() {
        Log.d(TAG , "entered icon unhidden");
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        getPackageManager().setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        Log.d(TAG, "App icon unhidden");
    }


}
