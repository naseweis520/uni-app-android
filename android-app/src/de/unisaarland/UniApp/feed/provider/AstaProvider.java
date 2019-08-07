package de.unisaarland.UniApp.feed.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.feed.provider.generic.GenericXmlFeedProvider;
import de.unisaarland.UniApp.feed.provider.generic.GenericRssArticlesExtractor;

public class AstaProvider extends GenericXmlFeedProvider {
    private Context context;

    public AstaProvider(Context context) {
        super(context, "https://asta.uni-saarland.de/feed/",
                new GenericRssArticlesExtractor("https://asta.uni-saarland.de/feed/"));
        this.context = context;
    }

    @Override
    public Drawable getDisplayIcon() {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_newspaper_white);
        Drawable wrapDrawable = drawable.mutate();
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(R.color.uni_blue_lighter));
        return wrapDrawable;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "AStA";
    }
}
