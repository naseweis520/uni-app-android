package de.unisaarland.UniApp.feed.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Date;

public abstract class FeedItem {
    public Context context;
    public FeedProvider feedProvider;

    public FeedItem(Context context, FeedProvider feedProvider) {
        this.context = context;
        this.feedProvider = feedProvider;
    }

    enum Priority {
        HIGH,
        NORMAL
    }

    @Nullable
    public Integer getBackgroundColor() {
        return null;
    }

    @NonNull
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Nullable
    public Date getPubDate() {
        return null;
    }

    @NonNull
    public abstract View getLayout(Context context);

    @Nullable
    public String getHeading() {
        return null;
    }
}
