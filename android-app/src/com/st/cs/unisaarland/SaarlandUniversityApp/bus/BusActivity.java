package com.st.cs.unisaarland.SaarlandUniversityApp.bus;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.model.PointOfInterest;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.model.SearchStationModel;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.uihelper.BusStationsAdapter;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.uihelper.SearchStationAdapter;
import com.st.cs.unisaarland.SaarlandUniversityApp.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/1/13
 * Time: 11:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusActivity extends Activity implements ConnectionCallbacks,LocationListener,OnConnectionFailedListener {
    private final int BUS_ID = 5;
    private ArrayList<PointOfInterest> busStationsArray = null;
    private Location currentLocation = null;
    private String provider = null;
    private ListView busStationsList = null;
    private BusStationsAdapter busStationAdapter = null;


    //////////////location //////////////
    private LocationClient locationClient;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(3000)         // 3 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    public void onPause() {
        super.onPause();
        if (locationClient != null) {
            locationClient.disconnect();
            locationClient = null;
        }
    }

    @Override
    protected void onStop() {
        if(busStationsArray != null){
            busStationsArray.clear();
        }
        busStationsArray = null;
        currentLocation = null;
        busStationsList = null;
        busStationAdapter = null;
        super.onStop();
    }

    @Override
    protected void onResume() {
        setUpLocationClientIfNeeded();
        locationClient.connect();
        super.onResume();
    }

    private void setUpLocationClientIfNeeded() {
        if (locationClient == null) {
            locationClient = new LocationClient(
                    this,
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBar();
        setContentView(R.layout.bus_layout);
    }

    @Override
    protected void onStart() {
        busStationsArray = new ArrayList<PointOfInterest>();
        updateModel();
        super.onStart();
    }

    private void updateModel(){
        DatabaseHandler dbHandler = new DatabaseHandler(this);
        ArrayList<PointOfInterest> tempBusStations = dbHandler.getPointsOfInterestForCategoryWithID(BUS_ID);
        dbHandler.close();
        HashMap<String,String> tempHashMap = new HashMap<String, String>(5);
        for(PointOfInterest poi: tempBusStations){
            if(!tempHashMap.containsKey(poi.getTitle())){
                busStationsArray.add(poi);
                tempHashMap.put(poi.getTitle(),poi.getTitle());
            }
        }
        populateItems();
    }

    private void populateItems() {
        busStationsList = (ListView) findViewById(R.id.bus_stations_list_view);
        busStationAdapter = new BusStationsAdapter(this,busStationsArray,currentLocation,provider);
        busStationsList.setAdapter(busStationAdapter);

        /// for search stations
        ListView searchStationsList = (ListView) findViewById(R.id.search_list_view);
        ArrayList<SearchStationModel> searchStationArray = new ArrayList<SearchStationModel>();
        SearchStationModel searchStationModel = new SearchStationModel();
        searchStationModel.setName("Search a bus");
        searchStationModel.setURL("Bahn.de");
        searchStationArray.add(searchStationModel);
        searchStationsList.setAdapter(new SearchStationAdapter(this,searchStationArray));
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.navigation_bar_layout);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

        TextView pageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_heading);
        pageText.setText(R.string.busText);
        pageText.setVisibility(View.VISIBLE);
        pageText.setTextColor(Color.BLACK);

        TextView backPageText = (TextView) actionBar.getCustomView().findViewById(R.id.page_back_text);
        backPageText.setText(R.string.homeText);
        backPageText.setVisibility(View.VISIBLE);
        backPageText.setOnClickListener(new BackButtonClickListener(this));

        ImageButton backButton = (ImageButton) actionBar.getCustomView().findViewById(R.id.back_icon);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new BackButtonClickListener(this));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
    }

    @Override
    public void onDisconnected() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        busStationAdapter.setCurrentLocation(currentLocation);
        busStationsList.invalidateViews();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    class BackButtonClickListener implements View.OnClickListener{
        final Activity activity;
        public BackButtonClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            activity.onBackPressed();
        }
    }
}