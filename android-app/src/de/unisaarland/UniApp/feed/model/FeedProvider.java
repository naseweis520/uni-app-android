package de.unisaarland.UniApp.feed.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

public abstract class FeedProvider {
    public Context context;

    public FeedProvider(Context context) {
        this.context = context;
    }

    public Drawable getDisplayIcon() {
        return null;
    }

    @NonNull
    public abstract String getDisplayName();

    public abstract void pollAsync(IFeedPoller feedPoller, Character token, Integer max);
}
