package com.mercury.onemusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class AudioPlayerService extends Service {

    public static final String LOG_TAG = "AudioPlayerService";
    public static final int STATE_STANDBY = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;

    private MediaPlayer mPlayer;
    private int state;
    private Notification notification;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.studio_session);
        state = STATE_STANDBY;
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(MainActivity.COMPLETE_PLAYING).setType("text/*"));
                state = STATE_STANDBY;
                stopForeground(true);
            }
        });
    }

    public void startPlaying() {
        mPlayer.start();
        state = STATE_PLAYING;
        notification = new Notification(R.drawable.notification_template_icon_bg, getText(R.string.status_playing),
                System.currentTimeMillis());
        /*Notification.Builder notificationBuilder=new Notification.Builder(getApplicationContext());
        notif*/
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        notification.setLatestEventInfo(getApplicationContext(), getText(R.string.app_name),
                getText(R.string.status_playing), pendingIntent);
        startForeground(1, notification);
    }

    public void pause() {
        mPlayer.pause();
        state = STATE_PAUSED;
        notification.setLatestEventInfo(getApplicationContext(), getText(R.string.app_name),
                getText(R.string.status_paused), pendingIntent);
        startForeground(1, notification);
    }

    public int getState() {
        return state;
    }

    public boolean isPlaying() {
        return state == STATE_PLAYING;
    }

    public boolean isPaused() {
        return state == STATE_PAUSED;
    }

    public boolean isStandby() {
        return state == STATE_STANDBY;
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        stopForeground(false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return new AudioPlayerServiceBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!isPlaying()) {
            stopForeground(true);
            mPlayer.release();
        }

        return super.onUnbind(intent);
    }
}
