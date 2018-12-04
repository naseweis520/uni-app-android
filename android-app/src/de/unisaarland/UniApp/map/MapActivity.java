package de.unisaarland.UniApp.map;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.GroundOverlay2;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.campus.uihelper.SearchAdapter;

public class MapActivity extends AppCompatActivity {
    MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Map
        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Campus overlay
        GroundOverlay2 overlay = new GroundOverlay2();
        overlay.setTransparency(0.05f);
        overlay.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.overlays_saarbruecken_2016_04));
        overlay.setPosition(new GeoPoint(49.2599078622207,7.03353523939848), new GeoPoint( 49.24951497610306, 7.052352887091693));
        map.getOverlayManager().add(overlay);

        // Location
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getBaseContext()), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            locationOverlay.enableFollowLocation();
        });

        // Center map
        IMapController mapController = map.getController();
        mapController.setZoom(17.0);
        GeoPoint startPoint = new GeoPoint(49.25500, 7.04135);
        mapController.setCenter(startPoint);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume();
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.campus_search_activity, menu);
        //Setting up the search widget

        /* SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.activity_search));
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        // search.setSuggestionsAdapter(new SearchAdapter(this, db.getAllData(), this));

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
        }); */
        return true;
    }
}
