package com.frodo.travigator.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.frodo.travigator.R;
import com.frodo.travigator.adapter.StopListAdapter;
import com.frodo.travigator.adapter.TabsPagerAdapter;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.LocationChangedEvent;
import com.frodo.travigator.events.MocLocationChangedEvent;
import com.frodo.travigator.fragments.Favorite;
import com.frodo.travigator.fragments.HomeFragment;
import com.frodo.travigator.fragments.MapNavigationFragment;
import com.frodo.travigator.fragments.TextNavigationFragment;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.Constants;
import com.frodo.travigator.utils.LocationUtil;
import com.frodo.travigator.utils.MocLocSimulator;
import com.frodo.travigator.utils.PrefManager;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class NavigateActivity extends FragmentActivity implements ActionBar.TabListener {
    public static final String STOPS = "stops";
    public static final String SRC_STOP = "src_stop";
    public static final String DST_STOP = "dst_stop";

    private ViewPager viewPager;
    private NavigateActivity.TabsPagerAdapter mAdapter;
    private ActionBar actionBar;

    // Tab titles
    private String[] tabs = {"Text", "Map"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewPager = new ViewPager(this);
        viewPager.setId(R.id.pager);

        setContentView(viewPager);

        actionBar = getActionBar();
        mAdapter = new NavigateActivity.TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LocationUtil.checkLocationPermission() && LocationUtil.isGPSOn()) {
            trApp.getLocationUtil().startLocationUpdates();
        } else if (!LocationUtil.checkLocationPermission()){
            trApp.getLocationUtil().askLocationPermission(this);
        } else {
            trApp.getLocationUtil().checkLocationSettings(this);
        }
    }

    @Override
    public void onBackPressed() {
        trApp.getLocationUtil().stopLocationUpdates();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LocationUtil.REQ_PERMISSIONS_REQUEST_ACCESS_FILE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    trApp.getLocationUtil().checkLocationSettings(this);
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationUtil.REQUEST_CHECK_SETTINGS:
                trApp.getLocationUtil().dialogClosed();
                trApp.getLocationUtil().startLocationUpdates();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    private class TabsPagerAdapter extends FragmentPagerAdapter {



        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {

            switch (index) {
                case 0:
                    return new TextNavigationFragment();
                case 1:
                    return new MapNavigationFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
