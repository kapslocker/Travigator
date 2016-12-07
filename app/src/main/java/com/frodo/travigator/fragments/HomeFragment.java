package com.frodo.travigator.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.frodo.travigator.R;
import com.frodo.travigator.activities.NavigateActivity;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.db.DbHelper;
import com.frodo.travigator.events.LocationChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;
import com.frodo.travigator.utils.Constants;
import com.frodo.travigator.utils.LocationUtil;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final String CITY_LIST = "city_list";
    private static final String ROUTE_LIST = "route_list";
    private static final String STOPS_LIST = "stops_list";

    public ArrayList<String> cityList, routeNoList, stopList;
    public Spinner citySpinner, routeSpinner, stopSpinner, srcStopSpinner;
    public String Route = "", City = "";
    public Stop[] stops;
    public int checker = 1;
    public int deboardPos = -1;
    public int srcPos = -1;
    private ProgressDialog mProgressDialog;

    private EditText ip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.home, container, false);

        if (savedInstanceState != null) {
            cityList = savedInstanceState.getStringArrayList(CITY_LIST);
            routeNoList = savedInstanceState.getStringArrayList(ROUTE_LIST);
            stops = (Stop[])savedInstanceState.getSerializable(STOPS_LIST);
            stopList = (ArrayList)CommonUtils.getStringArray(stops);
        } else {
            cityList = new ArrayList<String>();
            routeNoList = new ArrayList<String>();
            stopList = new ArrayList<String>();
            cityList.add(getString(R.string.selectCity));
            routeNoList.add(getString(R.string.cityFirst));
            stopList.add(getString(R.string.routeFirst));
        }

        citySpinner = (Spinner) rootView.findViewById(R.id.citySpinner);
        routeSpinner = (Spinner) rootView.findViewById(R.id.routeNoSpinner);
        stopSpinner = (Spinner) rootView.findViewById(R.id.stopSpinner);
        srcStopSpinner = (Spinner) rootView.findViewById(R.id.srcStopSpinner);


        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, cityList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);
        citySpinner.setOnItemSelectedListener(cityListener);

        if (cityList.size() == 2) {
            citySpinner.setSelection(1);
        }

        ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, routeNoList);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);
        routeSpinner.setOnItemSelectedListener(routeListener);

        ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stopList);
        stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stopSpinner.setAdapter(stopAdapter);
        stopSpinner.setOnItemSelectedListener(stopListener);
        srcStopSpinner.setOnItemSelectedListener(srcStopListener);


        Button navigate = (Button) rootView.findViewById(R.id.searchNavigate);
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (City == "") {
                    CommonUtils.toast(getString(R.string.selectCity));
                } else if (Route == "") {
                    CommonUtils.toast(getString(R.string.selectRoute));
                } else {
                    if (isFavorite(Route)) {
                        loadStops(true);
                    } else {
                        favAlert();
                    }
                }
            }
        });

        Button refresh = (Button) rootView.findViewById(R.id.searchRefresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });
        return rootView;
    }

    private void showProgressDialog(String title, String message) {
        if (getActivity() == null)
            return;
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(getActivity(), title, message);
            mProgressDialog.setIndeterminate(true);
        } else {
            mProgressDialog.setTitle(title);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        //MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                CommonUtils.openSettings(getContext());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(CITY_LIST, cityList);
        outState.putStringArrayList(ROUTE_LIST, routeNoList);
        outState.putSerializable(STOPS_LIST, stops);
    }

    private OnItemSelectedListener cityListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            routeNoList.clear();
            routeNoList.trimToSize();

            if (pos == 0) {
                routeNoList.add(getString(R.string.cityFirst));
                City = "";
                routeSpinner.setEnabled(false);
            } else {
                routeNoList.add(getString(R.string.selectRoute));
                City = CommonUtils.deCapitalize(cityList.get(pos));
                JsonArrayRequest objRequest = new JsonArrayRequest(Constants.SERVER_ROOT + "get_routes/" + City,
                        new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jArray) {
                        CommonUtils.log(jArray.toString());
                        if (getActivity() == null)
                            return;
                        mProgressDialog.dismiss();
                        try {
                            for (int i = 0; i<jArray.length(); i++) {
                                JSONObject obj = jArray.getJSONObject(i);
                                String temp = obj.getString("route");
                                routeNoList.add(temp);
                                routeSpinner.setEnabled(true);
                            }
                        }catch (Exception ex) {
                            CommonUtils.toast("Unable to parse server response");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (getActivity() == null)
                            return;
                        mProgressDialog.dismiss();
                        CommonUtils.toast(error.toString());
                    }
                });
                trApp.getRequestQueue().add(objRequest);
                showProgressDialog("Loading...", getContext().getString(R.string.loadingRoute));
            }

            ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, routeNoList);
            routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeSpinner.setAdapter(routeAdapter);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            routeNoList.clear();
            routeNoList.trimToSize();

            routeNoList.add(getString(R.string.cityFirst));

            City = "";

            routeSpinner.setEnabled(false);

            ArrayAdapter<String> routeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, routeNoList);
            routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeSpinner.setAdapter(routeAdapter);

        }
    };

    private OnItemSelectedListener routeListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            stopList.clear();
            stopList.trimToSize();
            Route = "";

            if (pos != 0) {
                stopList.add(getString(R.string.selectStop));
                stopSpinner.setEnabled(true);
                srcStopSpinner.setEnabled(true);
                Route = routeNoList.get(pos);
                loadStops(false);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stopList);
                ArrayAdapter<String> srcAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, stopList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                srcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stopSpinner.setAdapter(adapter);
                srcStopSpinner.setAdapter(srcAdapter);
            } else {
                stopList.add(getString(R.string.routeFirst));
                stopSpinner.setEnabled(false);
                srcStopSpinner.setEnabled(false);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stopList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ArrayAdapter<String> srcAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stopList);
                srcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stopSpinner.setAdapter(adapter);
                srcStopSpinner.setAdapter(srcAdapter);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Route = "";

            stopList.clear();
            stopList.trimToSize();

            stopList.add(getString(R.string.routeFirst));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stopList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stopSpinner.setAdapter(adapter);
        }
    };

    private OnItemSelectedListener srcStopListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            srcPos = position-1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            srcPos = -1;
        }
    } ;

    private OnItemSelectedListener stopListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            deboardPos = pos - 1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            deboardPos = -1;
        }
    };


    public void addFav() {
        if (City == "") {
            CommonUtils.toast(getString(R.string.selectCity));
        } else if (Route == ""){
            CommonUtils.toast(getString(R.string.selectRoute));
        }

        DbHelper db = new DbHelper(getContext(),City, "route_"+CommonUtils.deCapitalize(Route));
        db.setTable(stops);
        db.closeDB();
    }

    private boolean isFavorite(String route) {
        DbHelper db = new DbHelper(getContext(),City);
        Cursor c = db.getTables();

        Boolean flag = false;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String temp = c.getString(0);
                if (temp.toLowerCase().equals("route_"+route.toLowerCase())) {
                    flag = true;
                    break;
                }
                c.moveToNext();
            }
        }
        db.closeDB();

        return flag;
    }


    public void favAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Add to Favorites?");

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to add this route to your Favorite?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                addFav();
                loadStops(true);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                loadStops(true);
            }
        });

        alertDialog.show();
    }

    public void init() {
        cityList.clear();
        cityList.trimToSize();
        cityList.add(getString(R.string.selectCity));
        JsonArrayRequest objReq = new JsonArrayRequest(Constants.SERVER_ROOT + "get_cities",
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jArray) {
                CommonUtils.log(jArray.toString());
                if (getActivity() == null)
                    return;
                mProgressDialog.dismiss();
                try {
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject obj = jArray.getJSONObject(i);
                        cityList.add(CommonUtils.capitalize(obj.getString("name")));
                    }
                }catch (Exception ex) {
                    CommonUtils.toast("Error parsing response from server! Please try again");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtils.log(error.toString());
                CommonUtils.toast(error.toString());
                if (getActivity() == null)
                    return;
                mProgressDialog.dismiss();
            }
        });
        trApp.getRequestQueue().add(objReq);
        showProgressDialog("Loading...", getContext().getString(R.string.loadingCity));
    }

    private void loadStops(final boolean isNavigate ) {
        if (stops == null) {
            JsonArrayRequest objRequest = new JsonArrayRequest(Constants.SERVER_ROOT + "get_stops/" + CommonUtils.deCapitalize(City) + "?route=" + Route,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jArray) {
                            if (getActivity() == null)
                                return;
                            mProgressDialog.dismiss();
                            stops = new Gson().fromJson(jArray.toString(), new Stop[]{}.getClass());
                            if (stops == null) {
                                CommonUtils.toast("Unable to parse server response");
                                return;
                            }
                            for (int i = 0; i < stops.length; i++) {
                                stopList.add(stops[i].getStop_name());
                            }
                            if (LocationUtil.checkLocationPermission() && LocationUtil.isGPSOn()) {
                                trApp.getLocationUtil().startLocationUpdates();
                            } else if (!LocationUtil.checkLocationPermission()) {
                                trApp.getLocationUtil().askLocationPermission(getActivity());
                            } else {
                                trApp.getLocationUtil().checkLocationSettings(getActivity());
                            }
                            CommonUtils.toast("Getting your location. Please wait...");
                            if (isNavigate) {
                                if (srcPos == -1 || deboardPos == -1) {
                                    CommonUtils.toast("Please select source and destination");
                                    return;
                                }
                                Intent navitaionActivity = new Intent(getActivity(), NavigateActivity.class);
                                navitaionActivity.putExtra(NavigateActivity.STOPS, stops);
                                navitaionActivity.putExtra(NavigateActivity.SRC_STOP, srcPos);
                                navitaionActivity.putExtra(NavigateActivity.DST_STOP, deboardPos);
                                startActivity(navitaionActivity);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getActivity() == null)
                        return;
                    mProgressDialog.dismiss();
                    CommonUtils.toast(error.toString());
                }
            });
            trApp.getRequestQueue().add(objRequest);
            showProgressDialog("Loading...", getContext().getString(R.string.loadingStop));
        } else {
            if (srcPos == -1 || deboardPos == -1) {
                CommonUtils.toast("Please select source and destination");
                return;
            }
            Intent navitaionActivity = new Intent(getActivity(), NavigateActivity.class);
            navitaionActivity.putExtra(NavigateActivity.STOPS, stops);
            navitaionActivity.putExtra(NavigateActivity.SRC_STOP, srcPos);
            navitaionActivity.putExtra(NavigateActivity.DST_STOP, deboardPos);
            startActivity(navitaionActivity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onLocationChangedEvent(LocationChangedEvent event) {
        if (stops == null || stops.length <= 0) {
            return;
        }
        if (srcStopSpinner.getSelectedItemPosition() > 0)
            return;
        int pos = CommonUtils.getStopPos(stops, event.getLocation());
        srcStopSpinner.setSelection(pos+1);
        if (pos == -1 && checker == 1) {
                checker = 2;
//            CommonUtils.toast("You are not near any bus stop on this route");
            trApp.getTTS().speak("You are not near any bus stop on this route.", TextToSpeech.QUEUE_FLUSH, null);
            return;
        }
    }
}