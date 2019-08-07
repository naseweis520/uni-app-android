package de.unisaarland.UniApp.map.uihelper;

import android.content.Intent;
import android.net.Uri;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import de.unisaarland.UniApp.R;

public class MapInfoWindow extends InfoWindow {
    public Integer color;
    public float latitude;
    public float longitude;
    public String title;
    public String subtitle;
    public String url;
    private MapView mapView;

    public MapInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        this.mapView = mapView;
    }

    @Override
    public void onOpen(Object item) {
        // Clear InfoWindows
        MapInfoWindow.closeAllInfoWindowsOn(mapView);

        // Color
        if(color != null) {
            ConstraintLayout constraintLayout_bubble = mView.findViewById(R.id.bubble_layout);
            constraintLayout_bubble.setBackgroundColor(color);
        }

        // Navigate
        ImageView imageView_navigate = mView.findViewById(R.id.imageView_navigate);
        if (latitude == 0 || longitude == 0) {
            imageView_navigate.setVisibility(View.GONE);
        } else {
            imageView_navigate.setOnClickListener(v -> {
                Intent navIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:" + latitude + "," + longitude));

                mapView.getContext().startActivity(navIntent);
            });
        }
        imageView_navigate.setOnLongClickListener(v -> {
            Toast.makeText(mapView.getContext(), R.string.open_location_in_external_app, Toast.LENGTH_SHORT).show();
            return true;
        });

        // Title
        TextView textview_title = mView.findViewById(R.id.textview_title);
        textview_title.setText(title == null ? "Uni Campus" : title);

        // Subtitle
        TextView textview_subtitle = mView.findViewById(R.id.textview_subtitle);
        if (subtitle == null || subtitle.equals("")) {
            textview_subtitle.setVisibility(View.GONE);
        } else {
            textview_subtitle.setText(subtitle);
        }

        // Url
        ImageView imageView_url = mView.findViewById(R.id.imageView_url);
        if (url == null || url.equals("")) {
            imageView_url.setVisibility(View.GONE);
        } else {
            imageView_url.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mapView.getContext().startActivity(browserIntent);
            });
        }
    }

    @Override
    public void onClose() {

    }
}
