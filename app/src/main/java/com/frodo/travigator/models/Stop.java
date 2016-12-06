package com.frodo.travigator.models;

import java.io.Serializable;

/**
 * Created by durgesh on 4/29/16.
 */
public class Stop implements Serializable {
    public Stop(){}
    public Stop(String name, String pos, String lat, String lon) {
        this.stop_name = name;
        this.stop_lat = lat;
        this.stop_lon = lon;
        this.stop_pos = pos;
    }
    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_pos() {
        return stop_pos;
    }

    public void setStop_pos(String stop_pos) {
        this.stop_pos = stop_pos;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public double getStop_lat() {
        return Double.valueOf(stop_lat);
    }

    public void setStop_lat(String stop_lat) {
        this.stop_lat = stop_lat;
    }

    public double getStop_lon() {
        return Double.valueOf(stop_lon);
    }

    public void setStop_lon(String stop_lon) {
        this.stop_lon = stop_lon;
    }

    private String stop_id, stop_pos, stop_name, stop_lat, stop_lon;
}
