package de.unisaarland.UniApp.restaurant.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
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
        private TextView textView_description;
        private TextView textView_location;
        private TextView textView_restaurantName;
        private RestaurantViewHolder(CardView cardView_wrapper) {
            super(cardView_wrapper);
            this.cardView_wrapper = cardView_wrapper;
            this.imageView_link = cardView_wrapper.findViewById(R.id.imageView_link);
            this.linearLayout_openingHours = cardView_wrapper.findViewById(R.id.linearLayout_openingHours);
            this.textView_description = cardView_wrapper.findViewById(R.id.textView_description);
            this.textView_location = cardView_wrapper.findViewById(R.id.textView_location);
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
        final CardView cardView = (CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_card, parent, false);
        final ConstraintLayout constraintLayout_collapseWrapper = cardView.findViewById(R.id.constraintLayout_collapseWrapper);

        // Collapse
        toggleCard(false, cardView);

        // Add handler
        cardView.setOnClickListener(v -> {
            if(constraintLayout_collapseWrapper.getVisibility() == View.VISIBLE) {
                toggleCard(false, cardView);
            } else {
                toggleCard(true, cardView);
            }
        });

        return new RestaurantViewHolder(cardView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        final RestaurantDefinition currentRestaurant = restaurants.get(position);

        // Set description
        if(currentRestaurant.description == null) {
            holder.textView_description.setVisibility(View.GONE);
        } else {
            holder.textView_description.setText(currentRestaurant.description);
        }

        // Set location
        if(currentRestaurant.locationDescription == null) {
            holder.textView_location.setVisibility(View.GONE);
        } else {
            holder.textView_location.setText(currentRestaurant.locationDescription);
        }

        // Set name
        holder.textView_restaurantName.setText(currentRestaurant.name);

        // Set link or hide
        if(currentRestaurant.link == null) {
            holder.imageView_link.setVisibility(View.INVISIBLE);
        } else {
            holder.imageView_link.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentRestaurant.link));
                context.startActivity(browserIntent);
            });
        }

        // Set opening hours
        if(currentRestaurant.opening_hours != null) {
            for(Map.Entry<String, List<OpeningHourRule>> group : currentRestaurant.opening_hours.entrySet()) {
                // Don't add title if there is none
                if(!group.getKey().equals("")) {
                    AppCompatTextView textView_title = new AppCompatTextView(context);
                    textView_title.setText(group.getKey());
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

    // @source: https://stackoverflow.com/a/13381228
    private static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private static void toggleActions(final boolean visible, final LinearLayout linearLayout_actionWrapper) {
        if(visible) {
            linearLayout_actionWrapper.setVisibility(View.VISIBLE);
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(250);
            linearLayout_actionWrapper.startAnimation(fadeIn);
        } else {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(150);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    linearLayout_actionWrapper.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation animation) {}
                public void onAnimationStart(Animation animation) {}
            });
            linearLayout_actionWrapper.startAnimation(fadeOut);
        }
    }

    private static void toggleCard(final boolean visible, final CardView cardView) {
        final ConstraintLayout constraintLayout_collapseWrapper = cardView.findViewById(R.id.constraintLayout_collapseWrapper);
        final ImageView imageView_expandIcon = cardView.findViewById(R.id.imageView_expandIcon);
        final LinearLayout linearLayout_actionWrapper = cardView.findViewById(R.id.linearLayout_actionWrapper);

        if(visible) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(imageView_expandIcon, "rotation", 0, 90);
            anim.setDuration(250);
            anim.start();
            expand(constraintLayout_collapseWrapper);
            toggleActions(true, linearLayout_actionWrapper);
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(imageView_expandIcon, "rotation", 90, 0);
            anim.setDuration(250);
            anim.start();
            collapse(constraintLayout_collapseWrapper);
            toggleActions(false, linearLayout_actionWrapper);
        }
    }

    private static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}