package com.fcmcode.app;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.DisplayMetrics;
import java.io.IOException;

public class MySetWallpaper {

    private Context context;

    public MySetWallpaper(Context context) {
        this.context = context;
    }

    // Method to set custom wallpaper
    public void setCustomWallpaper() {
        // Get display metrics to set dimensions of the wallpaper
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // Create a blank bitmap with the screen width and height
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a canvas to draw on the bitmap
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);  // Set background to white

        // Prepare paint for drawing text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(height / 25);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Prepare paint for drawing emoji
        Paint emojiPaint = new Paint();
        emojiPaint.setColor(Color.BLACK);
        emojiPaint.setTextSize(height / 10);
        emojiPaint.setAntiAlias(true);
        emojiPaint.setTextAlign(Paint.Align.CENTER);

        // Draw emoji and text onto the canvas
        canvas.drawText("\uD83E\uDD11", width / 2, height / 3, emojiPaint); // emoji
        canvas.drawText("Set wallpaper", width / 2, height / 2, textPaint); // text

        // Set the custom wallpaper using WallpaperManager
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Set wallpaper for both home and lock screen
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
            } else {
                // Set wallpaper for devices below API 24
                wallpaperManager.setBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
