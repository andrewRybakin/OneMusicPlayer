package com.mercury.onemusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG="MainActivity";
    public static final String COMPLETE_PLAYING = "playingComplete";

    private Button actionButton;
    private TextView statusLabel;
    private AudioPlayerService mService;
    private Intent serviceIntent;
    private SeekBar seekBar;
    private AudioManager aManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setStatusLabelState(mService.getState());
            }
        }, IntentFilter.create(COMPLETE_PLAYING, "text/*"));

        ServiceConnection aPConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((AudioPlayerServiceBinder) service).getService();
                setStatusLabelState(mService.getState());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        serviceIntent = new Intent(this, AudioPlayerService.class);

        aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initUI();

        //Чтоб каждый раз при повороте не совершалась попытка запуска сервиса...
        //Ну не нравится мне, что при повороте он пытается запустить итак запущеный сервис. Тестировал - вроде не влияет на работоспособность
        if(savedInstanceState==null)startService(serviceIntent);
        bindService(serviceIntent, aPConnection, 0);
    }

    private void setStatusLabelState(int state) {
        switch (state) {
            case AudioPlayerService.STATE_STANDBY:
                statusLabel.setText(R.string.status_standby);
                actionButton.setText(R.string.action_play);
                break;
            case AudioPlayerService.STATE_PLAYING:
                statusLabel.setText(R.string.status_playing);
                actionButton.setText(R.string.action_pause);
                break;
            case AudioPlayerService.STATE_PAUSED:
                statusLabel.setText(R.string.status_paused);
                actionButton.setText(R.string.action_continue);
                break;
        }
    }

    private void initUI() {
        actionButton = (Button) findViewById(R.id.action_button);
        statusLabel = (TextView) findViewById(R.id.status_label);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isStandby() || mService.isPaused())
                    mService.startPlaying();
                else if (mService.isPlaying())
                    mService.pause();
                setStatusLabelState(mService.getState());
            }
        });
        seekBar=(SeekBar)findViewById(R.id.volume_seekbar);
        seekBar.setMax(aManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(aManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                aManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                aManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if((keyCode==KeyEvent.KEYCODE_VOLUME_UP)||(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN)){
            seekBar.setProgress(aManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
        return super.onKeyDown(keyCode, event);
    }
}
