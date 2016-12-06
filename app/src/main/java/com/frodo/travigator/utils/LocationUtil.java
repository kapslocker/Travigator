package com.frodo.travigator.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.LocationChangedEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by durgesh on 4/29/16.
 */
public class LocationUtil implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult> {
    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 300;

    public static final int REQ_PERMISSIONS_REQUEST_ACCESS_FILE_LOCATION = 100;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private boolean mRequestingLocationUpdates;
    private LatLng mCurrentLocation;
    private String mLastUpdateTime;
    private final String TAG = LocationUtil.class.getSimpleName();
    private Context mContext;
    private boolean isDialogShowing;
    private Activity activity;

    public LocationUtil(Context context){

        mContext = context;
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        mGoogleApiClient.connect();
        startLocationUpdates();
    }

    public boolean isLocationAvaliable() {
        return mCurrentLocation != null ;
    }

    public static boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(trApp.getAppContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return permissionCheck == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static void askLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSIONS_REQUEST_ACCESS_FILE_LOCATION);
    }

    public boolean ismRequestingLocationUpdates(){
        return mRequestingLocationUpdates;
    }

    public boolean isDialogShowing(){
        return isDialogShowing;
    }

    public void dialogClosed(){
        isDialogShowing = false;
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    public void startLocationUpdates() {
        CommonUtils.log(TAG + ":here it is" + mGoogleApiClient.isConnected());
        if (!checkLocationPermission()) {
            CommonUtils.log(TAG+":No location permissions");
            return;
        }
        if(mGoogleApiClient.isConnected()) {
            CommonUtils.log(TAG+":startLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mRequestingLocationUpdates = true;
                }
            });
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        CommonUtils.log("Stopping Location Updates");
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mRequestingLocationUpdates = false;
                }
            });
        }
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    public void checkLocationSettings(Activity activity) {
        if(isDialogShowing)
            return;
        if(activity != null) {
            isDialogShowing = true;
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(
                            mGoogleApiClient,
                            mLocationSettingsRequest
                    );
            this.activity = activity;
            result.setResultCallback(this);
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized void buildGoogleApiClient() {
        CommonUtils.logStatus(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        startLocationUpdates();
        if (mCurrentLocation == null) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                return;
            }
            mCurrentLocation = new LatLng(location.getLatitude(),location.getLongitude());
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        CommonUtils.logStatus(TAG, "Location updated ->"+location.getLatitude()+":"+location.getLongitude());
        EventBus.getDefault().post(new LocationChangedEvent(mCurrentLocation));
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                //dialogClosed();
                CommonUtils.logStatus(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                CommonUtils.logStatus(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    if(activity != null)
                        status.startResolutionForResult(activity, LocationUtil.REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    CommonUtils.logStatus(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                CommonUtils.logStatus(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    public LatLng getLocation(){
        return mCurrentLocation;
    }

    public LocationUtil setActivity(Activity activity){
        this.activity = activity;
        return this;
    }

    public String getmLastUpdateTime() {
        return mLastUpdateTime;
    }

    public static boolean isGPSOn() {
        LocationManager locationManager = (LocationManager) trApp.getAppContext()
                .getSystemService(trApp.getAppContext().LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ;
    }
}

