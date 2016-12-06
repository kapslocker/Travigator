package com.frodo.travigator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.frodo.travigator.R;
import com.frodo.travigator.activities.NavigateActivity;
import com.frodo.travigator.adapter.StopListAdapter;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.LocationChangedEvent;
import com.frodo.travigator.events.MocLocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.LocationUtil;
import com.frodo.travigator.utils.MocLocSimulator;
import com.frodo.travigator.utils.PrefManager;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by durgesh on 5/10/16.
 */
public class TextNavigationFragment extends Fragment {

    private ListView stopsList ;
    private StopListAdapter stopListAdapter;
    private Stop[] stops;
    private int[] status;
    private int srcPos, dstPos, current = -1;
    private boolean isFirstTimeAdjusted = false;
    private int infoGivenPos = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.text_navigation_fragment, null);
        stopsList = (ListView)rootView.findViewById(R.id.stop_list);
        status = new int[stops.length];
        if (srcPos <= dstPos) {
            for (int i = 0 ; i < stops.length ; i++) {
                if (i < srcPos || i > dstPos)
                    status[i] = StopListAdapter.STATUS_INACTIVE;
                else
                    status[i] = StopListAdapter.STATUS_REMANING;
            }
        } else  {
            for (int i = stops.length-1 ; i >= 0 ; i--) {
                if (i > srcPos || i < dstPos)
                    status[i] = StopListAdapter.STATUS_INACTIVE;
                else
                    status[i] = StopListAdapter.STATUS_REMANING;
            }
        }
        stopListAdapter = new StopListAdapter(getContext(), stops, status);
        stopsList.setAdapter(stopListAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        stops = (Stop[]) getActivity().getIntent().getSerializableExtra(NavigateActivity.STOPS);
        if (stops == null) {
            getActivity().finish();
        }
        srcPos = getActivity().getIntent().getIntExtra(NavigateActivity.SRC_STOP, -1);
        dstPos = getActivity().getIntent().getIntExtra(NavigateActivity.DST_STOP, -1);

        if (LocationUtil.checkLocationPermission() && LocationUtil.isGPSOn()) {
            trApp.getLocationUtil().startLocationUpdates();
        } else if (!LocationUtil.checkLocationPermission()){
            trApp.getLocationUtil().askLocationPermission(getActivity());
        } else {
            trApp.getLocationUtil().checkLocationSettings(getActivity());
        }
        CommonUtils.toast("Getting your location. Please wait...");
    }

    @Subscribe
    public void onLocationChangedEvent(LocationChangedEvent event) {
        LatLng latLng = event.getLocation();
        int pos = CommonUtils.getStopPos(stops, latLng);
        CommonUtils.log("Pos:"+pos);
        if (pos == -1 && !isFirstTimeAdjusted){
            isFirstTimeAdjusted = true;
            stopsList.smoothScrollToPosition(CommonUtils.getNearstStop(stops, latLng));
        } else {
            if (pos == -1) {
                if(current != -1) {
                    status[current] = StopListAdapter.STATUS_VISITED;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopListAdapter.changeStatus(status);
                            }
                        });
                    }
                }
                return;
            }
            stopsList.smoothScrollToPosition(pos);
            if (infoGivenPos != pos) {
                String message = "You arrived at " + stops[pos].getStop_name()+".";
                if (dstPos == pos) {
                    message = message+". This is you final stop.";
                }
                if (PrefManager.isTTSEnabled()) {
                    trApp.getTTS().speak(message, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
            infoGivenPos = pos;
            isFirstTimeAdjusted = false;
            if (current == -1)
                current = pos;
            if (current != pos) {
                status[current] = StopListAdapter.STATUS_VISITED;
            }
            current = pos;
            status[current] = StopListAdapter.STATUS_CURRENT;
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopListAdapter.changeStatus(status);
                    }
                });
            }
        }
    }

    @Subscribe
    public void onMocLocationChangedEvent(MocLocationChangedEvent event) {
        CommonUtils.log("Event: "+event.getLocation().toString());
        EventBus.getDefault().post(new LocationChangedEvent(event.getLocation()));
    }
}
