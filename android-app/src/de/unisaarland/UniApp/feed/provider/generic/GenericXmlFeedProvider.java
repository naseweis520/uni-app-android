package de.unisaarland.UniApp.feed.provider.generic;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.unisaarland.UniApp.feed.model.FeedItem;
import de.unisaarland.UniApp.feed.model.IFeedPoller;
import de.unisaarland.UniApp.feed.model.FeedProvider;
import de.unisaarland.UniApp.utils.ContentExtractor;
import de.unisaarland.UniApp.utils.NetworkRetrieveAndCache;
import de.unisaarland.UniApp.utils.Util;

public abstract class GenericXmlFeedProvider extends FeedProvider {
    private Context context;
    private ContentExtractor contentExtractor;
    private IFeedPoller feedPoller = null;
    private NetworkRetrieveAndCache<List<RssArticleData>> fetcher = null;
    private Character token;
    private String url;

    public GenericXmlFeedProvider(Context context, String url, ContentExtractor contentExtractor) {
        super(context);
        this.contentExtractor = contentExtractor;
        this.context = context;
        this.url = url;
    }

    @Override
    public abstract Drawable getDisplayIcon();

    @Override
    @NonNull
    public abstract String getDisplayName();

    @Override
    public void pollAsync(IFeedPoller feedPoller, Character token, Integer max) {
        // @TODO: Manage max
        assert(max == null || max >= 0);
        this.token = token;
        fetchFeed(feedPoller, this.url, max);
    }

    private void fetchFeed(IFeedPoller feedPoller, String url, Integer max) {
        this.feedPoller = feedPoller;
        if (fetcher == null) {
            String tag = "rss-"+Integer.toHexString(url.hashCode());
            fetcher = new NetworkRetrieveAndCache<List<RssArticleData>>(url, tag,  Util.getContentCache(context),
                    contentExtractor, new GenericXmlFeedProvider.NetworkDelegate(max),
                    context);
        }
        fetcher.loadAsynchronously(15 * 60);
    }

    private class NetworkDelegate implements NetworkRetrieveAndCache.Delegate<List<RssArticleData>> {
        private Integer max;

        NetworkDelegate(Integer max) {
            this.max = max;
        }

        @Override
        public void onUpdate(List<RssArticleData> feedList, boolean fromCache) {
            feedPoller.setProviderState(IFeedPoller.ProviderState.Finished, token);

            List<FeedItem> rssArticleFeedItemList = new ArrayList<>();
            for (RssArticleData rssArticleData: feedList) {
                rssArticleFeedItemList.add(new RssArticleFeedItem(context, GenericXmlFeedProvider.this, rssArticleData));
            }

            if(max != null && rssArticleFeedItemList.size() > max) {
                // Order list
                Collections.sort(rssArticleFeedItemList, (feedItem, t1) -> {
                    if(feedItem.getPubDate() == null && t1.getPubDate() == null) {
                        return 0;
                    } else if(feedItem.getPubDate() == null) {
                        return -1;
                    } else if(t1.getPubDate() == null) {
                        return 1;
                    } else {
                        return feedItem.getPubDate().compareTo(t1.getPubDate());
                    }
                });

                // Trim list
                rssArticleFeedItemList = rssArticleFeedItemList.subList(rssArticleFeedItemList.size() - 1 - max, rssArticleFeedItemList.size() - 1);
            }

            feedPoller.addAsyncLoadedFeedItems(rssArticleFeedItemList, token);
        }

        @Override
        public void onStartLoading() {
            feedPoller.setProviderState(IFeedPoller.ProviderState.Working, token);
        }

        @Override
        public void onFailure(String message) {
            feedPoller.setProviderState(IFeedPoller.ProviderState.Failed, token);
        }

        @Override
        public String checkValidity(List<RssArticleData> result) {
            return null;
        }
    }
}
