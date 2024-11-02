package com.fcmcode.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class RingAlarm {
    private static final String TAG = "RingAlarm";
    private Context context;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Handler volumeHandler;
    private Runnable volumeRunnable;
    private boolean isAlarmPlaying = false;

    public RingAlarm(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.volumeHandler = new Handler(Looper.getMainLooper());
    }

    public void startAlarm() {
        if (isAlarmPlaying) {
            Log.d(TAG, "Alarm is already playing");
            return;
        }

        try {
            // Release any existing MediaPlayer
            releaseMediaPlayer();

            // Get the default alarm sound
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            if (alarmUri == null) {
                Log.e(TAG, "No alarm sound found");
                return;
            }

            // Set up MediaPlayer with alarm sound
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, alarmUri);

            // Set audio attributes for alarm
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mediaPlayer.setAudioAttributes(attributes);

            // Set volume to maximum before playing
            setMaxVolume();

            mediaPlayer.setLooping(true);

            // Prepare asynchronously
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    isAlarmPlaying = true;
                    startVolumeControl();
                    Log.d(TAG, "Alarm started successfully");
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                    stopAlarm();
                    return true;
                }
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Error starting alarm: " + e.getMessage());
            e.printStackTrace();
            releaseMediaPlayer();
        }
    }

    private void setMaxVolume() {
        try {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error setting volume: " + e.getMessage());
        }
    }

    private void startVolumeControl() {
        volumeRunnable = new Runnable() {
            @Override
            public void run() {
                setMaxVolume();
                if (isAlarmPlaying) {
                    volumeHandler.postDelayed(this, 500);
                }
            }
        };
        volumeHandler.post(volumeRunnable);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                isAlarmPlaying = false; // Ensure alarm flag is reset
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer: " + e.getMessage());
            }
        }
    }

    public void stopAlarm() {
        isAlarmPlaying = false;

        // Stop volume control
        if (volumeHandler != null && volumeRunnable != null) {
            volumeHandler.removeCallbacks(volumeRunnable);
            volumeHandler = null;
            volumeRunnable = null;
        }

        releaseMediaPlayer();
        Log.d(TAG, "Alarm stopped");
    }
}
