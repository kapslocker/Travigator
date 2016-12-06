package com.frodo.travigator.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by durgesh on 5/3/16.
 */
public class MocLocationChangedEvent {
    private LatLng location;

    public MocLocationChangedEvent(LatLng location) {
        this.location = location;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
