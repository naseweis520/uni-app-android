package de.unisaarland.UniApp.feed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.feed.model.FeedAdapter;
import de.unisaarland.UniApp.feed.model.FeedCategory;
import de.unisaarland.UniApp.feed.model.FeedItem;
import de.unisaarland.UniApp.feed.model.IFeedPoller;
import de.unisaarland.UniApp.feed.model.FeedProvider;
import de.unisaarland.UniApp.feed.provider.AstaProvider;
import de.unisaarland.UniApp.feed.provider.FachschaftsratInformatikProvider;
import de.unisaarland.UniApp.feed.provider.HizProvider;

public class FeedFragment extends Fragment implements IFeedPoller {
    private List<FeedProvider> feedProviderList;
    private Dictionary<Character, FeedProvider> feedPollTokenDictionary;
    private FeedItemManager feedItemManager = new FeedItemManager();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_fragment, container, false);

        feedProviderList = new ArrayList<>();
        feedProviderList.add(new AstaProvider(getContext()));
        feedProviderList.add(new FachschaftsratInformatikProvider(getContext()));
        feedProviderList.add(new HizProvider(getContext()));

        // Setup recyclerview
        recyclerView = view.findViewById(R.id.recyclerView_feed);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new FeedAdapter(getContext(), feedItemManager);
        recyclerView.setAdapter(recyclerViewAdapter);

        feedPollTokenDictionary = new Hashtable<>();
        for (FeedProvider feedProvider: feedProviderList) {
            char generatedToken = generateToken(feedProvider);
            // @TODO: Manage tokens
            // @TODO: Manage max
            feedProvider.pollAsync(this, generatedToken, 5);
        }

        return view;
    }

    @Override
    public void addAsyncLoadedFeedItems(List<FeedItem> feedList, Character token) {
        int adapterPosition = feedItemManager.size();
        feedItemManager.addAll(FeedCategory.MIDDLE, feedList);      // @TODO
        recyclerViewAdapter.notifyItemRangeInserted(adapterPosition, feedList.size());
    }

    public char generateToken(FeedProvider feedProvider) {
        Random rand = new Random();

        char result;
        do {
            result = (char)rand.nextInt(Character.MAX_VALUE);
        } while(feedPollTokenDictionary.get(result) != null);

        feedPollTokenDictionary.put(result, feedProvider);
        return result;
    }

    @Override
    public void setProviderState(ProviderState state, Character token) {
        // @TODO: Manage loading state
    }
}