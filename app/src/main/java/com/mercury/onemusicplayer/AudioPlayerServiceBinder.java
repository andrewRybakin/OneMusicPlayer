package com.mercury.onemusicplayer;

import android.os.Binder;

public class AudioPlayerServiceBinder extends Binder {
    private AudioPlayerService service;

    public AudioPlayerServiceBinder (AudioPlayerService service){
        this.service=service;
    }

    public AudioPlayerService getService(){
        return service;
    }
}
