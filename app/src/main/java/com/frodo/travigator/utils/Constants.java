package com.frodo.travigator.utils;

/**
 * Created by durgesh on 4/28/16.
 */
public class Constants {
    public static final String SERVER_IP = "ec2-54-187-129-120.us-west-2.compute.amazonaws.com";
    public static final String SERVER_ROOT = "http://"+ SERVER_IP + "/Nav/public/index.php/app/";
    public static final int ERROR_RADIUS = 50;
    public static final float MAP_ZOOM = 15;

    public static class Pref {
        public static final String NAME = "Preferences";
        public static final String TTS = "tts";
    }
}
