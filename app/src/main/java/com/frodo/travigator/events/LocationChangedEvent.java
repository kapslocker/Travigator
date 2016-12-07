package com.frodo.travigator.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by durgesh on 4/29/16.
 */
public class LocationChangedEvent {
    private LatLng location;

    public LocationChangedEvent(LatLng location) {
        this.location = location;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
