package com.frodo.travigator.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.frodo.travigator.R;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.models.Stop;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by durgesh on 4/28/16.
 */
public class CommonUtils {
    public static void log(String message) {
        Log.i("Log", message);
    }

    public static void logStatus(String key, String message) {
        Log.i(key, message);
    }

    public static void toast(String message) {
        Toast.makeText(trApp.getAppContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String message) {
        Toast.makeText(trApp.getAppContext(), message, Toast.LENGTH_LONG).show();
    }

    public static String capitalize(String text) {
        char[] ch = text.toLowerCase().toCharArray();
        boolean found = true;

        for (int i = 0; i < ch.length; i++) {
            if (found && Character.isLetter(ch[i])) {
                ch[i] = Character.toUpperCase(ch[i]);
                found = false;
            } else if (Character.isWhitespace(ch[i]) || ch[i] == '_') {
                found = true;
            }
        }

        return String.valueOf(ch).replaceAll("_", " ").trim();
    }

    public static String deCapitalize(String text) {
        text = text.toLowerCase();
        text = text.replace(' ', '_');
        return text;
    }

    public static List<String> getStringArray(Stop[] stops) {
        List<String> stopList = new ArrayList<>();
        for (Stop stop : stops) {
            stopList.add(stop.getStop_name());
        }
        return stopList;
    }

    public static int getStopPos(Stop[] stops, LatLng latLng) {
        int pos = CommonUtils.getNearstStop(stops, latLng);
        Stop stop = stops[pos];
        float[] result = new float[2];
        Location.distanceBetween(latLng.latitude, latLng.longitude, stop.getStop_lat(), stop.getStop_lon(), result);
        if (result[0] < Constants.ERROR_RADIUS)
            return pos;
        return -1;
    }

    public static int getNearstStop(Stop[] stops, LatLng latLng) {
        int res = 0;
        float distance = Float.MAX_VALUE;
        for (int i = 0 ; i < stops.length ; i++) {
            Stop stop = stops[i];
            float[] result = new float[2];
            Location.distanceBetween(latLng.latitude, latLng.longitude, stop.getStop_lat(), stop.getStop_lon(), result);
            if (distance > result[0]){
                distance = result[0];
                res = i;
            }
            //CommonUtils.log("Distance:"+i+":"+result[0]+"Pos:"+res);
        }
        return res;
    }

    public static void openSettings(final Context context) {
        final String disTTS[] ={"Enable/Disable TalkBack","Enable Text to Speech"};
        final String enTTS[] = {"Enable/Disable TalkBack","Disable Text to Speech", "Change text to speech settings"};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = (View) inflater.inflate(R.layout.list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Settings");

        final ListView lv = (ListView) convertView.findViewById(R.id.dialogList);
        ArrayAdapter<String> adapter;
        if (PrefManager.isTTSEnabled()) {
            adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,enTTS);
        }
        else {
            adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,disTTS);
        }
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (PrefManager.isTTSEnabled()){
                    switch (position){
                        case 0: Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            context.startActivity(intent);
                            break;
                        case 1: PrefManager.enableTTS(false);
                            ArrayAdapter<String> ad = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,disTTS);
                            lv.setAdapter(ad);
                            break;
                        case 2:intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                            intent.setAction("com.android.settings.TTS_SETTINGS");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            break;
                        //case 3: break;
                        default: break;
                    }
                }
                else {
                    switch (position){
                        case 0:	Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            context.startActivity(intent);
                            break;
                        case 1: PrefManager.enableTTS(true);
                            ArrayAdapter<String> ad = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,enTTS);
                            lv.setAdapter(ad);
                            break;
                        default: break;
                    }
                }
            }
        });


        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }
}
