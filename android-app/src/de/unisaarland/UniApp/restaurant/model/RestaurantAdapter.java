package de.unisaarland.UniApp.restaurant.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.restaurant.OpeningHoursActivity.CampusRestaurantsFragment.OpeningHourRule;
import de.unisaarland.UniApp.restaurant.OpeningHoursActivity.CampusRestaurantsFragment.RestaurantDefinition;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    public Context context;
    private List<RestaurantDefinition> restaurants;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private CardView cardView_wrapper;
        private ImageView imageView_link;
        private LinearLayout linearLayout_openingHours;
        private TextView textView_restaurantName;
        private RestaurantViewHolder(CardView cardView_wrapper) {
            super(cardView_wrapper);
            this.cardView_wrapper = cardView_wrapper;
            this.imageView_link = cardView_wrapper.findViewById(R.id.imageView_link);
            this.linearLayout_openingHours = cardView_wrapper.findViewById(R.id.linearLayout_openingHours);
            this.textView_restaurantName = cardView_wrapper.findViewById(R.id.textView_restaurantName);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RestaurantAdapter(List<RestaurantDefinition> restaurants) {
        this.restaurants = restaurants;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        CardView v = (CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.view_restaurants_card, parent, false);
        return new RestaurantViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        final RestaurantDefinition currentRestaurant = restaurants.get(position);

        // Set Restaurant name
        holder.textView_restaurantName.setText(currentRestaurant.name);

        // Set link or hide
        if(currentRestaurant.link == null) {
            holder.imageView_link.setVisibility(View.INVISIBLE);
        } else {
            holder.imageView_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRestaurant.link));
                    context.startActivity(browserIntent);
                }
            });
        }

        // Set opening hours
        if(currentRestaurant.opening_hours != null) {
            for(Map.Entry<String, List<OpeningHourRule>> group : currentRestaurant.opening_hours.entrySet()) {
                // Add title
                String title = group.getKey();
                if(group.getKey().equals("break")) title = context.getString(R.string.during_semester_break);
                if(group.getKey().equals("semester")) title = context.getString(R.string.during_semester);

                // Don't add title if there is none
                if(!group.getKey().equals("")) {
                    AppCompatTextView textView_title = new AppCompatTextView(context);
                    textView_title.setText(title);
                    textView_title.setTextColor(context.getResources().getColor(R.color.uni_blue));
                    textView_title.setTypeface(null, Typeface.BOLD);
                    holder.linearLayout_openingHours.addView(textView_title);
                }


                // Add rules
                TableLayout tableLayout_rules = new TableLayout(context);
                for(OpeningHourRule rule : group.getValue()) {
                    TableRow tableRow_rule = new TableRow(context);

                    // Left side
                    AppCompatTextView textView_left = new AppCompatTextView(context);
                    textView_left.setText(rule.x);
                    textView_left.setPadding(0, 0, 32, 0);
                    tableRow_rule.addView(textView_left);

                    // Right side
                    AppCompatTextView textView_right = new AppCompatTextView(context);
                    textView_right.setText(rule.y);
                    tableRow_rule.addView(textView_right);

                    tableLayout_rules.addView(tableRow_rule);
                }
                holder.linearLayout_openingHours.addView(tableLayout_rules);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return restaurants.size();
    }
}