package com.fcmcode.app;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

public class MyResetPassword extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private static final int ACTIVATE_TOKEN_REQUEST = 2;
    private SharedPreferences sharedPreferences;
    private static final String KEY_RESET_TOKEN = "reset_token";
    private DevicePolicyManager dpm;
    private ComponentName adminComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        setResetPasswordToken();


    }

    private void setResetPasswordToken() {
        Log.d("ResetPassword ayushi", "setResetPasswordToken called");

        if (!dpm.isAdminActive(adminComponent)) {
            Log.d("ResetPassword ayushi", "Device Admin is not active");
            Toast.makeText(this, "Please enable Device Admin first", Toast.LENGTH_SHORT).show();
            enableDeviceAdmin();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                boolean isEscrowTokenActive = dpm.isResetPasswordTokenActive(adminComponent);
                Log.d("ResetPassword ayushi", "Is escrow token active: " + isEscrowTokenActive);

                if (!isEscrowTokenActive) {
                    byte[] token = new byte[32];
                    new SecureRandom().nextBytes(token);

                    boolean success = dpm.setResetPasswordToken(adminComponent, token);
                    if (success) {
                        Log.d("ResetPassword ayushi", "Reset password token set successfully");
                        sharedPreferences.edit().putString(KEY_RESET_TOKEN, Base64.encodeToString(token, Base64.DEFAULT)).apply();
                        Toast.makeText(this, "Reset password token set successfully", Toast.LENGTH_SHORT).show();
                        activateToken();
                    } else {
                        Log.d("ResetPassword ayushi", "Failed to set reset password token");
                        Toast.makeText(this, "Failed to set reset password token", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("ResetPassword ayushi", "Escrow token is already active, proceeding to reset password");
                    resetLockScreenPassword();
                }
            } catch (SecurityException e) {
                Log.e("ResetPassword ayushi", "SecurityException: " + e.getMessage());
                Toast.makeText(this, "Security Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("ResetPassword ayushi", "Device does not support setResetPasswordToken");
            Toast.makeText(this, "Device does not support setResetPasswordToken", Toast.LENGTH_SHORT).show();
        }
    }
    private void enableDeviceAdmin() {
        if (!dpm.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need to enable this app as device admin to reset the lock screen password.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        } else {
            Toast.makeText(this, "Device Admin is already enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetLockScreenPassword() {
        if (dpm.isDeviceOwnerApp(getPackageName())) {
            String encodedToken = sharedPreferences.getString(KEY_RESET_TOKEN, null);
            if (encodedToken != null) {
                byte[] token = Base64.decode(encodedToken, Base64.DEFAULT);
                String newPassword = "111111";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        boolean result = dpm.resetPasswordWithToken(adminComponent, newPassword, token, 0);
                        if (result) {
                            Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                            dpm.lockNow();
                        } else {
                            Toast.makeText(this, "Failed to reset password with token", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("ResetPassword ayushi", "Error resetting password", e);
                        Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Device does not support resetPasswordWithToken", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Reset token not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "App is not the Device Owner", Toast.LENGTH_SHORT).show();
        }
    }

    private void activateToken() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Intent confirmIntent = keyguardManager.createConfirmDeviceCredentialIntent(null, "Please confirm your credentials to proceed.");

        if (confirmIntent != null) {
            startActivityForResult(confirmIntent, ACTIVATE_TOKEN_REQUEST);
        } else {
            resetLockScreenPassword();
        }
    }
}
