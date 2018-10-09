package de.unisaarland.UniApp.restaurant;

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
import de.unisaarland.UniApp.restaurant.model.RestaurantAdapter;

import static de.unisaarland.UniApp.restaurant.OpeningHoursActivity.CampusRestaurantsFragment.*;

// @TODO: Highlight restaurants which are open
// @TODO: Order restaurants based on whether they are open and their distance
public class OpeningHoursActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_opening_hours);

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
                        getString(R.string.Homburg)
                }));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.

                final String campus;
                switch(position) {
                    case 0:
                    default:
                        campus = "saarbrücken";
                        break;
                    case 1:
                        campus = "homburg";
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String campus = settings.getString(getString(R.string.pref_campus), null);
        if(campus.equals(getString(R.string.pref_campus_saar))) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_opening_hours, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
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

        public static CampusRestaurantsFragment newInstance(String campus) {
            CampusRestaurantsFragment fragment = new CampusRestaurantsFragment();
            Bundle args = new Bundle();
            args.putString(ARG_CAMPUS, campus);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            List<RestaurantDefinition> restaurantList = new ArrayList<>();

            switch(getArguments().getString(ARG_CAMPUS).toLowerCase()) {
                case "homburg":
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Mensa";
                        link = "http://www.studentenwerk-saarland.de/de/Verpflegung/Mensa-Campus-Homburg/Mensa";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("semester", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:00 - 14:30"));
                        } });
                        opening_hours.put("break", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:00 - 14:00"));
                        } });
                    } });
                    break;
                case "saarbrücken":
                default:
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Mensa";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/mensa.html";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("semester", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "11:30 - 14:30"));
                            add(new OpeningHourRule("Fr", "11:30 - 14:15"));
                            add(new OpeningHourRule("Mo-Fr (Freeflow)", "11:30 - 13:45"));
                        } });
                        opening_hours.put("break", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "11:30 - 14:15"));
                            add(new OpeningHourRule("Fr", "11:30 - 14:00"));
                            add(new OpeningHourRule("Mo-Fr (Freeflow)", "11:30 - 13:45"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Mensacafé";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/mensacafe.html";
                        locationAddress = "Building D4.1, Basement";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("semester", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "07:45 - 19:30"));
                            add(new OpeningHourRule("Fr", "07:45 - 14:45"));
                        } });
                        opening_hours.put("break", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Th", "07:45 - 15:00"));
                            add(new OpeningHourRule("Fr", "07:45 - 14:45"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Mensagarten";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/mensagarten.html";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("In Saison bei trockenem Wetter", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:00 - 15:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Sportlertreff";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/sportlertreff.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Ausländer-Café (AC)";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/ac-auslaender-cafe.html";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "09:00 - 18:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Philo-Café";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/philo-cafe.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Cafete";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/cafete.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "KHG-Café";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/khg-cafe.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Café unique";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/cafe-unique.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "iCoffee";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/icoffee.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Starbooks Coffee";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/starbooks-coffee.html";
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Fast Food Heroes";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/fast-food-heroes.html";
                        opening_hours = new LinkedHashMap<>();
                        opening_hours.put("semester", new ArrayList<OpeningHourRule>() { {
                            add(new OpeningHourRule("Mo-Fr", "11:30 - 16:00"));
                        } });
                    } });
                    restaurantList.add(new RestaurantDefinition() { {
                        name = "Campusmarkt";
                        link = "https://www.uni-saarland.de/studium/im/campus/essen/campusmarkt.html";
                    } });
            }

            View rootView = inflater.inflate(R.layout.fragment_openinghours, container, false);

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
