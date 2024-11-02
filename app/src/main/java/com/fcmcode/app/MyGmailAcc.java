package com.fcmcode.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyGmailAcc {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private Context context;

    public MyGmailAcc(Context context) {
        this.context = context;
    }


    /**
     * Check if we have the necessary permissions
     */
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the necessary permissions
     */
    public void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.GET_ACCOUNTS},
                PERMISSION_REQUEST_CODE);
    }

    /**
     * Get list of Gmail accounts
     * @return List of Gmail account emails
     */
    public List<String> getGmailAccounts() {
        List<String> gmailAccounts = new ArrayList<>();

        if (!checkPermission()) {
            return gmailAccounts;
        }

        try {
            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccountsByType("com.google");

            for (Account account : accounts) {
                if (account.name.endsWith("@gmail.com")) {
                    gmailAccounts.add(account.name);
                    Log.d("ayushi","ayushi" + gmailAccounts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gmailAccounts;
    }

    /**
     * Handle permission result
     */
    public boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}