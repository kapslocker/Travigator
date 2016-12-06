package com.frodo.travigator.utils;

import android.os.AsyncTask;

import com.frodo.travigator.events.MocLocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by durgesh on 5/3/16.
 */
public class MocLocSimulator {
    private Stop[] mStops;
    private int count = 0;
    private Timer timer;
    private int src = 0;
    private int dest = 0;
    public MocLocSimulator(int src, int dest) {
        this.src = src;
        this.dest = dest;
        this.count = src;
    }
    public void simulate(Stop[] stops, long intervel) {
        mStops = stops;
        timer = new Timer();
        if (src == -1)
            src = 0;
        if (dest == -1)
            dest = stops.length;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (count >= mStops.length || count > dest) {
                    timer.cancel();
                    return;
                }
                Stop stop = mStops[count];
                EventBus.getDefault().post(new MocLocationChangedEvent(new LatLng(stop.getStop_lat(), stop.getStop_lon())));
                count++;
            }
        }, intervel, intervel);
    }
}
