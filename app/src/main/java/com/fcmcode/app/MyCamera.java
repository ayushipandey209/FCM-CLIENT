package com.fcmcode.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class MyCamera extends Activity {
    private static final int REQUEST_OVERLAY_PERMISSION = 1000;


    private static final int CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("camera entered " , "cjfvef cjbb");
        super.onCreate(savedInstanceState);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "This device does not have a camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required if the app is in the background
                startActivity(intent);
            } else {
                // Permission denied - handle this scenario
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
        }
    }


}
