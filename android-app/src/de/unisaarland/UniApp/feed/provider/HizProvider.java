package de.unisaarland.UniApp.feed.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.feed.provider.generic.GenericRssArticlesExtractor;
import de.unisaarland.UniApp.feed.provider.generic.GenericXmlFeedProvider;

public class HizProvider extends GenericXmlFeedProvider {
    private Context context;

    public HizProvider(Context context) {
        super(context, "https://www.hiz-saarland.de/index.php?id=425&type=9818",
                new GenericRssArticlesExtractor("https://www.hiz-saarland.de/index.php?id=425&type=9818"));
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
        return context.getString(R.string.hochschul_it_zentrum);
    }
}
