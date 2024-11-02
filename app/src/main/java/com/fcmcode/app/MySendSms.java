package com.fcmcode.app;

import android.telephony.SmsManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class MySendSms {

    public void sendSms(String phoneNumber, String message, Context context) {
        try {
            Log.d("enter ayushi" ,"hello");
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "SMS sent successfully.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "SMS failed to send, please try again.", Toast.LENGTH_LONG).show();
        }
    }
}
