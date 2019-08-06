package de.unisaarland.UniApp.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisaarland.UniApp.feed.model.FeedCategory;
import de.unisaarland.UniApp.feed.model.FeedItem;

public class FeedItemManager {
    private List<FeedItemList> feedCategories;
    private int size;

    public FeedItemManager() {
        feedCategories = new ArrayList<>();

        // Add categories from array
        for(FeedCategory ignored : FeedCategory.values()) {
            feedCategories.add(new FeedItemList());
        }
    }

    public void add(FeedCategory category, FeedItem feedItem) {
        feedCategories.get(category.ordinal()).add(feedItem);
        size++;
    }

    public void addAll(FeedCategory category, Collection<FeedItem> feedItemList) {
        for(FeedItem feedItem : feedItemList) {
            add(category, feedItem);
        }
    }

    public FeedItem get(int index) {
        if(index < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            int target = index;
            for(ArrayList<FeedItem> feedItemList : feedCategories) {
                if(target < 0) {
                    throw new ArrayIndexOutOfBoundsException();
                } else {
                    if(target < feedItemList.size()) {
                        return feedItemList.get(target);
                    } else {
                        target -= feedItemList.size();
                    }
                }
            }
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public int size() {
        return size;
    }

    public class FeedItemList extends ArrayList<FeedItem> {
        @Override
        public boolean add(FeedItem newFeedItem) {
            if(this.size() >= 1 && newFeedItem.getPubDate() != null) {
                int i = 0;
                while(i < this.size()) {
                    FeedItem currentFeedItem = this.get(i);
                    if(currentFeedItem.getPubDate() == null || newFeedItem.getPubDate().compareTo(currentFeedItem.getPubDate()) > 0) {
                        this.add(i, newFeedItem);
                        return true;
                    } else {
                        i++;
                    }
                }
                return super.add(newFeedItem);
            } else {
                return super.add(newFeedItem);
            }
        }
    }
}