package de.unisaarland.UniApp.feed.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.feed.provider.generic.GenericRssArticlesExtractor;
import de.unisaarland.UniApp.feed.provider.generic.GenericXmlFeedProvider;

public class FachschaftsratInformatikProvider extends GenericXmlFeedProvider {
    private Context context;

    public FachschaftsratInformatikProvider(Context context) {
        // @TODO: Support english feed
        super(context, "https://cs.fs.uni-saarland.de/?feed=rss2&lang=de",
                new GenericRssArticlesExtractor("https://cs.fs.uni-saarland.de/?feed=rss2&lang=de"));
        this.context = context;
    }

    @Override
    public Drawable getDisplayIcon() {
        // @TODO: Custom icon
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_newspaper_white);
        Drawable wrapDrawable = drawable.mutate();
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(R.color.uni_blue_lighter));
        return wrapDrawable;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Fachschaftsrat Informatik";
    }
}
