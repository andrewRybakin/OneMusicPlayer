package com.mercury.onemusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String COMPLETE_PLAYING="playingComplete";

    private Button actionButton;
    private TextView statusLabel;
    private AudioPlayerService mService;
    private Intent serviceIntent;

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

        ServiceConnection aPConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService=((AudioPlayerServiceBinder) service).getService();
                setStatusLabelState(mService.getState());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        serviceIntent=new Intent(this, AudioPlayerService.class);

        initUI(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mService.isStandby()||mService.isPaused())
                    mService.startPlaying();
                else if(mService.isPlaying())
                    mService.pause();

                setStatusLabelState(mService.getState());
            }
        });

        startService(serviceIntent);
        bindService(serviceIntent, aPConnection, 0);
    }

    private void setStatusLabelState(int state){
        switch(state){
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

    private void initUI(View.OnClickListener listenerForActionButton){
        actionButton=(Button)findViewById(R.id.action_button);
        statusLabel=(TextView)findViewById(R.id.status_label);
        actionButton.setOnClickListener(listenerForActionButton);
    }
}
