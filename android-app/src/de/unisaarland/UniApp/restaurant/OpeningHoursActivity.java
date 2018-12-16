package de.unisaarland.UniApp.restaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.about.AboutActivity;
import de.unisaarland.UniApp.restaurant.MensaMenuActivity.Campuses;
import de.unisaarland.UniApp.restaurant.model.RestaurantAdapter;
import de.unisaarland.UniApp.settings.SettingsActivity;

import static de.unisaarland.UniApp.restaurant.OpeningHoursActivity.CampusRestaurantsFragment.*;

// @TODO: Highlight restaurants which are open
// @TODO: Order restaurants based on whether they are open and their distance
public class OpeningHoursActivity extends AppCompatActivity {
    public static final String BUNDLE_CAMPUS_KEY = "campus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.restaurant_openinghours_activity);

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
                        campus = Campuses.Homburg;
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, newInstance(campus))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Auto-select campus based on preferences
        // or display given campus if extras contain `campus`
        if(extras != null && extras.containsKey(BUNDLE_CAMPUS_KEY)) {
            // Bundle contains `campus`. Set spinner
            switch(Campuses.values()[extras.getInt(BUNDLE_CAMPUS_KEY)]) {
                case Homburg:
                    spinner.setSelection(1);
                    break;
                case Saarbruecken:
                default:
                    spinner.setSelection(0);
            }
        } else {
            // Bundle does not contain `campus`. Load campus from preferences
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            String campus = settings.getString(getString(R.string.pref_campus), null);
            if(campus.equals(getString(R.string.pref_campus_saar))) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_settings:
                startActivity(new Intent(OpeningHoursActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(OpeningHoursActivity.this, AboutActivity.class));
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
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        CampusAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
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

        public static CampusRestaurantsFragment newInstance(Campuses campus) {
            CampusRestaurantsFragment fragment = new CampusRestaurantsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_CAMPUS, campus.ordinal());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            List<RestaurantDefinition> restaurantList = new ArrayList<>();

            switch(Campuses.values()[getArguments().getInt(ARG_CAMPUS)]) {
                case Homburg:
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.mensa);
                        link = "http://www.studentenwerk-saarland.de/de/Verpflegung/Mensa-Campus-Homburg/Mensa";
                        locationDescription = getContext().getString(R.string.building_x, "74");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_semester), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:00 - 14:30"));
                        } });
                        opening_hours.put(getContext().getString(R.string.during_semester_break), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:00 - 14:00"));
                        } });
                    } });
                    break;
                case Saarbruecken:
                default:
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.mensa);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/mensa.html";
                        locationDescription = getContext().getString(R.string.building_x, "D4.1");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_semester), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "11:30 - 14:30"));
                            add(new OpeningHourRule("Fr", "11:30 - 14:15"));
                            add(new OpeningHourRule("Mo-Fr (Freeflow)", "11:30 - 13:45"));
                        } });
                        opening_hours.put(getContext().getString(R.string.during_semester_break), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "11:30 - 14:15"));
                            add(new OpeningHourRule("Fr", "11:30 - 14:00"));
                            add(new OpeningHourRule("Mo-Fr (Freeflow)", "11:30 - 13:45"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.mensacafe);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/mensacafe.html";
                        locationDescription = getContext().getString(R.string.building_x, "D4.1") + ", " + getContext().getString(R.string.basement);
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_semester), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "07:45 - 19:30"));
                            add(new OpeningHourRule("Fr", "07:45 - 14:45"));
                        } });
                        opening_hours.put(getContext().getString(R.string.during_semester_break), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "07:45 - 15:00"));
                            add(new OpeningHourRule("Fr", "07:45 - 14:45"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.mensagarten);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/mensagarten.html";
                        locationDescription = getContext().getString(R.string.meadow_behind_building_x, "A1.7");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_season_in_dry_weather), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:00 - 15:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.sportlertreff);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/sportlertreff.html";
                        locationDescription = getContext().getString(R.string.hermann_neuberger_school) + ", " + getContext().getString(R.string.building_x, "4");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Sa", "07:00 - 09:00, 11:30 - 22:00"));
                            add(new OpeningHourRule("Su", "07:30 - 09:00, 11:30 - 13:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.auslaendercafe);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/ac-auslaender-cafe.html";
                        locationDescription = getContext().getString(R.string.building_x, "A3.2");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "09:00 - 18:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.philo_cafe);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/philo-cafe.html";
                        locationDescription = getContext().getString(R.string.building_x, "C5.2");
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.cafete);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/cafete.html";
                        locationDescription = getContext().getString(R.string.audimax) + ", " + getContext().getString(R.string.building_x, "B4.1");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_semester), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "08:00 - 18:00"));
                            add(new OpeningHourRule("Fr", "08:00 - 16:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.khg_cafe);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/khg-cafe.html";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "11:00 - 16:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.cafe_unique);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/cafe-unique.html";
                        locationDescription = getContext().getString(R.string.campus_center) + ", " + getContext().getString(R.string.building_x, "A4.4");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "07:00 - 18:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.icoffee);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/icoffee.html";
                        locationDescription = getContext().getString(R.string.building_x, "E1.3");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "07:00 - 18:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.starbooks_coffee);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/starbooks-coffee.html";
                        locationDescription = getContext().getString(R.string.sulb) + ", " + getContext().getString(R.string.building_x, "B1.1");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "08:00 - 18:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.fast_food_heroes);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/fast-food-heroes.html";
                        locationDescription = getContext().getString(R.string.building_x, "C5.5");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_semester), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:30 - 16:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = getContext().getString(R.string.campusmarkt);
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/campusmarkt.html";
                        locationDescription = getContext().getString(R.string.building_x, "C5.5");
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put(getContext().getString(R.string.during_semester), new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "08:00 - 17:00"));
                            add(new OpeningHourRule("Fr", "08:00 - 16:00"));
                        } });
                    } });
            }

            View rootView = inflater.inflate(R.layout.restaurant_openinghours_fragment, container, false);

            recyclerView_restaurant = rootView.findViewById(R.id.recyclerView_restaurant);
            recyclerView_restaurant.setHasFixedSize(true);

            // Set layout manager
            layoutManager_restaurant = new LinearLayoutManager(getContext());
            recyclerView_restaurant.setLayoutManager(layoutManager_restaurant);

            // Set adapter
            adapter_restaurant = new RestaurantAdapter(restaurantList);
            ((RestaurantAdapter) adapter_restaurant).context = getContext();
            recyclerView_restaurant.setAdapter(adapter_restaurant);

            return rootView;
        }

        public static class RestaurantDefinition {
            public String name;
            public String description;
            public String link;
            // @TODO: Jump to campus map
            // public String coordinates;
            public String locationAddress;
            public String locationDescription;

            public Map<String, List<OpeningHourRule>> opening_hours;
        }

        public class OpeningHourRule {
            public final String x;
            public final String y;

            OpeningHourRule(String x, String y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
