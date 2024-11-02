package com.fcmcode.app;

import android.app.WallpaperManager;
import android.content.Context;
import android.util.Log;
import java.io.IOException;

public class MyRemoveWallpaper {

    private Context context;

    public MyRemoveWallpaper(Context context) {
        this.context = context;
    }

    public void clearWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {

            wallpaperManager.clear();
            Log.d("RemoveWallpaper", "Wallpaper removed and set to default.");
        } catch (IOException e) {
            Log.e("RemoveWallpaper", "Failed to remove wallpaper: " + e.getMessage());
        }
    }
}