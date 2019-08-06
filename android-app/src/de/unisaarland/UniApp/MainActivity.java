package de.unisaarland.UniApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import de.unisaarland.UniApp.about.AboutActivity;
import de.unisaarland.UniApp.bus.BusActivity;
import de.unisaarland.UniApp.feed.FeedFragment;
import de.unisaarland.UniApp.map.MapActivity;
import de.unisaarland.UniApp.restaurant.MensaMenuActivity;
import de.unisaarland.UniApp.restaurant.OpeningHoursActivity;
import de.unisaarland.UniApp.restaurant.notifications.MensaNotifications;
import de.unisaarland.UniApp.rssViews.RSSActivity;
import de.unisaarland.UniApp.settings.SettingsActivity;
import de.unisaarland.UniApp.staff.SearchStaffActivity;

/**
 * Launcher Activity of the application this Activity will be displayed when application is launched from the launcher
 */
public class MainActivity extends AppCompatActivity {
    private FeedFragment feedFragment;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // sets the custom navigation bar according to each activity.
        setContentView(R.layout.main);
        // set Listeners for the main screen to launch specific activity
        setButtonListeners();

        // Populate fragment container `@id/mainFragment` with content
        feedFragment = new FeedFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragment, feedFragment)
                .commit();

        // And set the mensa preferences (is not strictly needed, since each alarm should trigger
        // the next one, but for the case that something goes wrong...)
        new MensaNotifications(this).setNext();

        // Set bottomsheet event handlers
        ImageView imageView_swipeIndicator = findViewById(R.id.imageView_swipeIndicator);
        imageView_swipeIndicator.setImageDrawable(AnimatedVectorDrawableCompat.create(MainActivity.this, R.drawable.swipe_indicator_up2down));

        ScrollView scrollView_bottomSheet = findViewById(R.id.bottomsheet);
        bottomSheetBehavior = BottomSheetBehavior.from(scrollView_bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // @TODO: Set indicator
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // Set indicator icon
                    AnimatedVectorDrawableCompat swipeIndicatorDrawable = AnimatedVectorDrawableCompat.create(MainActivity.this, R.drawable.swipe_indicator_up2down);
                    imageView_swipeIndicator.setImageDrawable(swipeIndicatorDrawable);
                    swipeIndicatorDrawable.start();
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // Set indicator icon
                    AnimatedVectorDrawableCompat swipeIndicatorDrawable = AnimatedVectorDrawableCompat.create(MainActivity.this, R.drawable.swipe_indicator_down2up);
                    imageView_swipeIndicator.setImageDrawable(swipeIndicatorDrawable);
                    swipeIndicatorDrawable.start();

                    // Scroll navsheet to top if not there already
                    if (scrollView_bottomSheet.getScrollY() > 0) {
                        scrollView_bottomSheet.smoothScrollTo(0, 0);
                    }
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        RelativeLayout relativeLayout_swipeIndicatorWrapper = findViewById(R.id.relativeLayout_swipeIndicatorWrapper);
        relativeLayout_swipeIndicatorWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        // Detect first run and initially show bottomsheet and collapse it afterwards
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!settings.contains(getString(R.string.pref_campus))) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            Handler handler = new Handler();
            Runnable r = () -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            };
            handler.postDelayed(r, 500);
        }
    }

    /**
     * Will be called when activity created first time after onCreate or when activity comes to the front again or in a pausing state
     * So its better to set all the things needed to use in the activity here if in case anything is released in onPause method
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If this is the first start, show preferences...
        if (!settings.contains(getString(R.string.pref_campus))) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        // take special care in preceeding code to use default values on the settings, as they
        // might not have been set yet...

        // Set Text on the Mainscreen
        String campus = settings.getString(getString(R.string.pref_campus),
                getString(R.string.pref_campus_saar));
        TextView campusText = (TextView) findViewById(R.id.campusText);
        // unfortunately, campusText happens to be null sometimes.
        if (campusText != null) {
            int text = campus.equals(getString(R.string.pref_campus_saar))
                    ? R.string.c_saarbruecken : R.string.c_homburg;
            campusText.setText(text);
        }

        showWhatsNew(settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Handling the Action Bar Buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            finish();
        }
    }

    private void setButtonListeners() {

        Button newsButton = findViewById(R.id.newsBtn);
        newsButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, RSSActivity.class);
            myIntent.putExtra("category", RSSActivity.Category.News);
            MainActivity.this.startActivity(myIntent);
        });
        Button restaurantButton = findViewById(R.id.restaurantBtn);
        restaurantButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, MensaMenuActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
        restaurantButton.setOnLongClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, OpeningHoursActivity.class);
            MainActivity.this.startActivity(myIntent);
            return true;
        });
        Button campusButton = findViewById(R.id.campusBtn);
        campusButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, MapActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
        campusButton.setOnLongClickListener(v -> {
            // @TODO: Open map and then Search
            return true;
        });
        Button eventsButton = findViewById(R.id.eventsBtn);
        eventsButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, RSSActivity.class);
            myIntent.putExtra("category", RSSActivity.Category.Events);
            MainActivity.this.startActivity(myIntent);
        });
        Button busButton = findViewById(R.id.busBtn);
        busButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, BusActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
        Button staffSearchButton = findViewById(R.id.staffBtn);
        staffSearchButton.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, SearchStaffActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
    }

    private void showWhatsNew(SharedPreferences settings) {
        // Check whether we need to show the "what's new" dialog...
        int currentVersionNumber = BuildConfig.VERSION_CODE;
        int savedVersionNumber = settings.getInt(getString(R.string.pref_last_whatsnew_version), 0);
        if (currentVersionNumber == savedVersionNumber)
            return;

        int currentHash = getString(R.string.whatsnew_text).hashCode();
        int savedHash = settings.getInt(getString(R.string.pref_last_whatsnew_hash), 0);
        if (currentHash == savedHash)
            return;

        settings.edit()
                .putInt(getString(R.string.pref_last_whatsnew_version), currentVersionNumber)
                .putInt(getString(R.string.pref_last_whatsnew_hash), currentHash)
                .apply();

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.whatsnew_title)
                .setMessage(R.string.whatsnew_text)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).show();
    }
}