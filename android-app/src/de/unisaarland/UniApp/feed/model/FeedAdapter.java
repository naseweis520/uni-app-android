package de.unisaarland.UniApp.feed.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.unisaarland.UniApp.R;
import de.unisaarland.UniApp.feed.FeedItemManager;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Context context;
    private FeedItemManager feedItemManager;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class FeedViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView cardView;
        FeedItem feedItem;
        ImageView imageView_providerIcon;
        RelativeLayout relativeLayout_content;
        TextView textView_pubDate;
        TextView textView_providerName;
        TextView textView_title;
        FeedViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
            this.imageView_providerIcon = cardView.findViewById(R.id.imageView_providerIcon);
            this.relativeLayout_content = cardView.findViewById(R.id.relativeLayout_content);
            this.textView_pubDate = cardView.findViewById(R.id.textView_pubdate);
            this.textView_providerName = cardView.findViewById(R.id.textView_providerName);
            this.textView_title = cardView.findViewById(R.id.textView_title);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedAdapter(Context context, FeedItemManager feedItemManager) {
        this.context = context;
        this.feedItemManager = feedItemManager;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // @TODO: Different layouts (for example with image)
        final CardView cardView = (CardView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_itemcard, parent, false);
        return new FeedViewHolder(cardView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.feedItem = feedItemManager.get(position);
        if(holder.feedItem.getBackgroundColor() != null) {
            holder.cardView.setBackgroundColor(holder.feedItem.getBackgroundColor());
        }
        // Provider badge
        if(holder.feedItem.feedProvider.getDisplayIcon() == null) {
            holder.imageView_providerIcon.setVisibility(View.GONE);
        } else {
            holder.imageView_providerIcon.setImageDrawable(holder.feedItem.feedProvider.getDisplayIcon());
            holder.imageView_providerIcon.setVisibility(View.VISIBLE);
        }
        holder.textView_providerName.setText(holder.feedItem.feedProvider.getDisplayName());
        // Title
        if(holder.feedItem.getHeading() == null) {
            holder.textView_title.setVisibility(View.GONE);
        } else {
            holder.textView_title.setText(holder.feedItem.getHeading());
            holder.textView_title.setVisibility(View.VISIBLE);
        }
        // Content
        View contentView = holder.feedItem.getLayout(context);
        holder.relativeLayout_content.removeAllViews();
        holder.relativeLayout_content.addView(contentView);

        // Date
        if(holder.feedItem.getPubDate() == null) {
            holder.textView_pubDate.setVisibility(View.GONE);
        } else {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            holder.textView_pubDate.setText(dateFormat.format(holder.feedItem.getPubDate()));
            holder.textView_pubDate.setVisibility(View.VISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return feedItemManager.size();
    }

}
