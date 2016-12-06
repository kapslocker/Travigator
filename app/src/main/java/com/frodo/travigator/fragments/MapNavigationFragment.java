package com.frodo.travigator.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frodo.travigator.R;
import com.frodo.travigator.activities.NavigateActivity;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.LocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.Constants;
import com.frodo.travigator.utils.LocationUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by durgesh on 5/10/16.
 */
public class MapNavigationFragment extends Fragment {
    private GoogleMap mMap;
    private Stop[] stops;
    private Marker[] markers;
    private int srcPos, dstPos;
    private LatLng location;
    private Marker currentMarker;
    private Circle circle;
    private float currentZoom = Constants.MAP_ZOOM;
    private boolean isFirstTimeZoomChange = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_navigation_fragment, null);
        setUpMapIfNeeded();
        return rootView;
    }

    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.location_map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if (cameraPosition.zoom != currentZoom && !isFirstTimeZoomChange) {
                            currentZoom = cameraPosition.zoom;
                        }
                        isFirstTimeZoomChange = false;
                    }
                });
                markers = new Marker[stops.length];
                for (int i = 0 ; i < stops.length ; i++) {
                    Stop stop = stops[i];
                    markers[i] = (mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(stop.getStop_lat(), stop.getStop_lon()))
                            .icon(BitmapDescriptorFactory.defaultMarker())
                            .title(stop.getStop_name())));
                }
            }
        }
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

    @Subscribe
    public void onLocationChangedEvent(LocationChangedEvent event) {
        this.location = event.getLocation();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,
                        currentZoom));
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                if (circle != null) {
                    circle.remove();
                }
                currentMarker = mMap.addMarker(new MarkerOptions().title("your current location")
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                circle = mMap.addCircle(new CircleOptions().center(location).radius(Constants.ERROR_RADIUS));
            }
        });
    }
}
