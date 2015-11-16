package com.mercury.onemusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import java.io.FileDescriptor;

public class AudioPlayerService extends Service {

    public static final int STATE_STANDBY=0;
    public static final int STATE_PLAYING=1;
    public static final int STATE_PAUSED=2;

    private MediaPlayer mPlayer;
    private int state;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer=MediaPlayer.create(getApplicationContext(), R.raw.studio_session);
        state=STATE_STANDBY;
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(MainActivity.COMPLETE_PLAYING).setType("text/*"));
                state=STATE_STANDBY;
            }
        });
    }

    public void startPlaying(){
        mPlayer.start();
        state=STATE_PLAYING;
    }

    public void pause(){
        mPlayer.pause();
        state=STATE_PAUSED;
    }

    public int getState(){
        return state;
    }

    public boolean isPlaying(){
        return state==STATE_PLAYING;
    }

    public boolean isPaused(){
        return state==STATE_PAUSED;
    }

    public boolean isStandby(){
        return state==STATE_STANDBY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioPlayerServiceBinder(this);
    }
}
