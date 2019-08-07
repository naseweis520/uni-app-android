package de.unisaarland.UniApp.feed.provider.generic;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.feed.model.FeedItem;
import de.unisaarland.UniApp.feed.model.FeedProvider;

public class RssArticleFeedItem extends FeedItem {
    public RssArticleData rssArticleData;

    public RssArticleFeedItem(Context context, FeedProvider feedProvider, RssArticleData rssArticleData) {
        super(context, feedProvider);
        this.rssArticleData = rssArticleData;
    }

    @Override
    public Date getPubDate() {
        return rssArticleData.pubDate;
    }

    @NonNull
    @Override
    public View getLayout(Context context) {
        TextView textView = new TextView(context);
        // @TODO: Handle empty content
        if(rssArticleData.contentText == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(rssArticleData.contentText);
            textView.setVisibility(View.VISIBLE);
        }
        TextViewCompat.setTextAppearance(textView, R.style.FeedCardText);
        textView.setTextIsSelectable(true);
        return textView;
    }

    @Override
    public String getHeading() {
        return rssArticleData.heading;
    }
}
