package de.unisaarland.UniApp.map.uihelper;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.bus.model.PointOfInterest;
import de.unisaarland.UniApp.database.DatabaseHandler;
import de.unisaarland.UniApp.map.MapActivity;


public class SearchAdapter extends android.support.v4.widget.CursorAdapter {
    private final MapActivity parent;

    private final CategoryIconCache catIconCache;

    public SearchAdapter(Context context, Cursor cursor, MapActivity parent) {
        super(context, cursor, false);
        this.parent = parent;
        this.catIconCache = new CategoryIconCache(context.getAssets());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView itemTitle = view.findViewById(R.id.title);
        TextView itemDescription = view.findViewById(R.id.description);
        ImageView categoryIcon = view.findViewById(R.id.category_icon);
        String title = cursor.getString(1);
        String subtitle = cursor.getString(2);
        int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categorieID"));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
        itemTitle.setText(title);
        itemDescription.setText(subtitle);
        // use deprecated setBackgroundDrawable method because we want to support API <16
        categoryIcon.setBackgroundDrawable(catIconCache.getIconForCategory(categoryId));
        view.setOnClickListener(clickListener);
        view.setTag(R.id.campus_search_poi_id_tag, id);
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer id = (Integer) v.getTag(R.id.campus_search_poi_id_tag);
            DatabaseHandler db = new DatabaseHandler(parent);
            PointOfInterest model = db.getPOIsForIDs(Collections.singletonList(id)).get(0);
            db.close();
            if(model != null) {
                parent.searchItemSelected(model);
            }
        }
    };

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.map_search_row_layout, parent, false);
    }
}
