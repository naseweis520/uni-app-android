package de.unisaarland.UniApp.feed.model;

import java.util.List;

public interface IFeedPoller {
    enum ProviderState {
        Failed,
        Finished,
        Working
    }

    void addAsyncLoadedFeedItems(List<FeedItem> feedList, Character token);

    void setProviderState(ProviderState state, Character token);
}
