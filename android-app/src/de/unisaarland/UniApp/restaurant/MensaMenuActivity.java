package de.unisaarland.UniApp.restaurant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.uihelper.MensaMenuFragment;
import de.unisaarland.UniApp.settings.SettingsActivity;

public class MensaMenuActivity extends AppCompatActivity {

    public enum Campuses {
        Homburg,
        Mensagarten,
        Saarbruecken
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensa_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup spinner
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(new CampusAdapter(
                toolbar.getContext(),
                new String[]{
                        getString(R.string.saarbruecken),
                        getString(R.string.mensagarten),
                        getString(R.string.homburg)
                }));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.

                final Campuses campus;
                switch(position) {
                    case 0:
                    default:
                        campus = Campuses.Saarbruecken;
                        break;
                    case 1:
                        campus = Campuses.Mensagarten;
                        break;
                    case 2:
                        campus = Campuses.Homburg;
                }

                getSupportFragmentManager().beginTransaction()
                        // campus
                        .replace(R.id.container, MensaMenuFragment.newInstance(campus))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Auto-select campus based on preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String campus = settings.getString(getString(R.string.pref_campus), null);
        if(campus.equals(getString(R.string.pref_campus_saar))) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(2);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.restaurant_activity_icons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.show_settings:
                startActivity(new Intent(MensaMenuActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_opening_hours:
                //Open opening hours actions when button is pressed
                startActivity(new Intent(MensaMenuActivity.this, OpeningHoursActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private static class CampusAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final Helper mDropDownHelper;

        CampusAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if(convertView == null) {
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
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    public static class CampusRestaurantsFragment extends Fragment {
        private static final String ARG_CAMPUS = "campus";
        private RecyclerView recyclerView_restaurant;
        private RecyclerView.Adapter adapter_restaurant;
        private RecyclerView.LayoutManager layoutManager_restaurant;


        public CampusRestaurantsFragment() {
        }

        public static CampusRestaurantsFragment newInstance(String campus) {
            CampusRestaurantsFragment fragment = new CampusRestaurantsFragment();
            Bundle args = new Bundle();
            args.putString(ARG_CAMPUS, campus);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_opening_hours, container, false);

            return rootView;
        }
    }
}
