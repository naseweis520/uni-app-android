package de.unisaarland.UniApp.map;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.GroundOverlay2;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.database.DatabaseHandler;
import de.unisaarland.UniApp.map.uihelper.MapInfoWindow;
import de.unisaarland.UniApp.restaurant.MensaMenuActivity;

import static de.unisaarland.UniApp.restaurant.OpeningHoursActivity.BUNDLE_CAMPUS_KEY;

public class MapActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 5;
    private static final int REQUEST_CODE_LOCATION = 2;

    private static final String TAG = MapActivity.class.getSimpleName();
    private final Map<Marker, PointOfInterest> poisMap = new HashMap<>();
    private DatabaseHandler db = null;
    private MapView map = null;
    private Menu menu;

    private boolean askedForLocationPermission = false;
    private MyLocationNewOverlay locationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHandler(this);
        Bundle extras = getIntent().getExtras();

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.map_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Licence
        TextView licenceView = findViewById(R.id.map_licence);
        licenceView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.osm_licence_url)));
            startActivity(browserIntent);
        });

        // Permissions
        checkLocationUpdatesGranted();

        setupMap();

        // Setup spinner
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(new CampusAdapter(
                toolbar.getContext(),
                new String[]{
                        getString(R.string.saarbruecken),
                        getString(R.string.homburg),
                        getString(R.string.dudweiler),
                        getString(R.string.meerwiesertalweg)
                }));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                IMapController mapController = map.getController();

                locationOverlay.disableFollowLocation();
                // @TODO: Store in database
                switch (position) {
                    case 0:
                    default:
                        mapController.setZoom(17.0);
                        mapController.setCenter(new GeoPoint(49.25560, 7.04260));
                        break;
                    case 1:
                        mapController.setZoom(17.0);
                        mapController.setCenter(new GeoPoint(49.30700, 7.3450));
                        break;
                    case 2:
                        mapController.setZoom(19.0);
                        mapController.setCenter(new GeoPoint(49.27547, 7.03808));
                        break;
                    case 3:
                        mapController.setZoom(20.0);
                        mapController.setCenter(new GeoPoint(49.24151, 7.00599));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Auto-select campus based on preferences
        // or display given campus if extras contain `campus`
        if (extras != null && extras.containsKey(BUNDLE_CAMPUS_KEY)) {
            // Bundle contains `campus`. Set spinner
            switch (MensaMenuActivity.Campuses.values()[extras.getInt(BUNDLE_CAMPUS_KEY)]) {
                case Dudweiler:
                    spinner.setSelection(2);
                    break;
                case Homburg:
                    spinner.setSelection(1);
                case Saarbruecken:
                default:
                    spinner.setSelection(0);
                case Meerwiesertalweg:
                    spinner.setSelection(3);
            }
        } else {
            // Bundle does not contain `campus`. Load campus from preferences
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            String campus = settings.getString(getString(R.string.pref_campus), null);
            if (campus.equals(getString(R.string.pref_campus_saar))) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }
        }

        // if info building != null means activity is called from search result details page
        // so it will get the building position from the database and will set the marker there.
        final String infoBuilding = getIntent().getStringExtra("building");
        if (infoBuilding != null) {
            List<PointOfInterest> pois = db.getPointsOfInterestForTitle(infoBuilding);
            if (!pois.isEmpty())
                pinPoisInArray(pois);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && resultCode == RESULT_OK && data.getExtras() != null) {
            @SuppressWarnings("unchecked")
            List<PointOfInterest> pois = (List<PointOfInterest>) data.getExtras().get("pois");
            pinPoisInArray(pois);
        }
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume();
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_activity, menu);

        //Setting up the search widget
        this.menu = menu;

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.activity_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setSuggestionsAdapter(new de.unisaarland.UniApp.map.uihelper.SearchAdapter(this, db.getAllData(), this));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                loadData(query);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                loadData(query);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_categories:
                Intent myIntent = new Intent(MapActivity.this, MapSearchActivity.class);
                MapActivity.this.startActivityForResult(myIntent, REQUEST_CODE);
                return true;
            case R.id.action_clearmarker:
                clearPois();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupMap() {
        // Map
        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Add EventsOverlay
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                MapInfoWindow.closeAllInfoWindowsOn(map);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        map.getOverlays().add(0, mapEventsOverlay);

        // Overlays
        float overlayTransparency = 0.15f;
        addAssetOverlay("map/overlay/dudweiler.png", new GeoPoint(49.275693, 7.035003),
                new GeoPoint(49.273475, 7.040334), overlayTransparency);
        addAssetOverlay("map/overlay/homburg.png", new GeoPoint(49.312253, 7.337019),
                new GeoPoint(49.301333, 7.351746), overlayTransparency);
        addAssetOverlay("map/overlay/saarbruecken_2016_11.png", new GeoPoint(49.259810, 7.032793),
                new GeoPoint(49.250751, 7.052331), overlayTransparency);
        addAssetOverlay("map/overlay/sport.png", new GeoPoint(49.251483, 7.030290),
                new GeoPoint(49.249177, 7.039591), overlayTransparency);

        // Location
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getBaseContext()), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            locationOverlay.enableFollowLocation();
        });
    }

    private void addAssetOverlay(String path, GeoPoint topLeft, GeoPoint bottomRight, float transparency) {
        try {
            InputStream ims = getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            ims.close();

            GroundOverlay2 overlay = new GroundOverlay2();
            overlay.setTransparency(transparency);
            overlay.setImage(bitmap);
            overlay.setPosition(topLeft, bottomRight);
            map.getOverlayManager().add(overlay);
        } catch (IOException ignored) {
        }
    }

    private boolean checkLocationUpdatesGranted() {
        boolean granted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!granted && !askedForLocationPermission) {
            // Request missing location permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
            this.askedForLocationPermission = true;
        }

        return granted;
    }

    private void clearPois() {
        for (Marker m : poisMap.keySet()) {
            m.remove(map);
        }
    }

    private boolean pinPoisInArray(List<PointOfInterest> POIs) {
        if (POIs.isEmpty()) {
            // use exception to get stack trace
            Log.w(TAG, new IllegalStateException("empty POI list"));
            return false;
        }

        // map is null if Google Play services are not installed on the device
        if (map == null) {
            Log.w(TAG, new IllegalStateException("map not initiated"));
            return false;
        }

        for (PointOfInterest poi : POIs) {
            String tempColor = poi.getColor();
            /* @TODO: Implement
            float color = tempColor == 1 ? BitmapDescriptorFactory.HUE_CYAN
                    : tempColor == 2 ? BitmapDescriptorFactory.HUE_GREEN
                    : BitmapDescriptorFactory.HUE_RED;*/

            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(poi.getLatitude(), poi.getLongitude()));
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            newMarker.setTitle(poi.getTitle());
            MapInfoWindow infoWindow = new MapInfoWindow(R.layout.map_infowindow, map);
            // infoWindow.color = color;
            infoWindow.latitude = poi.getLatitude();
            infoWindow.longitude = poi.getLongitude();
            infoWindow.title = poi.getTitle();
            infoWindow.subtitle = poi.getSubtitle();
            infoWindow.url = poi.getWebsite();
            newMarker.setInfoWindow(infoWindow);
            map.getOverlays().add(newMarker);

            poisMap.put(newMarker, poi);
        }

        IMapController mapController = map.getController();
        BoundingBox boundingBox = getBoundingBox(poisMap);
        boundingBox = increaseBoundingboxByFactor(boundingBox, 0.25);
        if (poisMap.size() > 1) {
            map.zoomToBoundingBox(boundingBox, true);
        } else {
            mapController.animateTo(new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast()),
                    18.0, (long) 750.0);
        }
        return true;
    }

    private void loadData(String query) {
        //When the user input changes, the search results have to be adjusted
        Cursor cursor = db.getCursorPointsOfInterestPartialMatchedForSearchKey(query);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView search = (SearchView) menu.findItem(R.id.activity_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setSuggestionsAdapter(new de.unisaarland.UniApp.map.uihelper.SearchAdapter(this, cursor, this));
    }

    /**
     * source  https://stackoverflow.com/a/24122164
     */
    public BoundingBox getBoundingBox(Map<Marker, PointOfInterest> poisMap) {
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = Double.MIN_VALUE;

        for (Map.Entry<Marker, PointOfInterest> item : poisMap.entrySet()) {
            GeoPoint point = item.getKey().getPosition();
            if (point.getLatitude() < minLat)
                minLat = point.getLatitude();
            if (point.getLatitude() > maxLat)
                maxLat = point.getLatitude();
            if (point.getLongitude() < minLong)
                minLong = point.getLongitude();
            if (point.getLongitude() > maxLong)
                maxLong = point.getLongitude();
        }

        return new BoundingBox(maxLat, maxLong, minLat, minLong);
    }

    public BoundingBox increaseBoundingboxByFactor(BoundingBox bb, double factor) {
        return new BoundingBox(bb.getLatNorth() - (bb.getLatitudeSpan() * factor),
                bb.getLonEast() + (bb.getLongitudeSpan() * factor),
                bb.getLatSouth() + (bb.getLatitudeSpan() * factor),
                bb.getLonWest() - (bb.getLongitudeSpan() * factor));
    }

    public void searchItemSelected(PointOfInterest model) {
        clearPois();
        poisMap.clear();
        pinPoisInArray(Collections.singletonList(model));
        final SearchView search = (SearchView) menu.findItem(R.id.activity_search).getActionView();
        search.setQuery("", false);
        search.setIconified(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    private static class CampusAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        CampusAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }
}
