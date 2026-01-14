package com.example.bookstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private List<FeedItem> feedItems;

    public FeedAdapter(List<FeedItem> feedItems) { this.feedItems = feedItems; }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = feedItems.get(position);
        holder.userName.setText(item.getUserName());
        holder.actionText.setText(item.getActionText());
        holder.bookTitle.setText(item.getBookTitle());

        if (item.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(item.getRating());
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return feedItems.size(); }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView userName, actionText, bookTitle;
        RatingBar ratingBar;

        public FeedViewHolder(@NonNull View v) {
            super(v);
            userName = v.findViewById(R.id.tvFeedUser);
            actionText = v.findViewById(R.id.tvFeedAction);
            bookTitle = v.findViewById(R.id.tvFeedBook);
            ratingBar = v.findViewById(R.id.feedRatingBar);
        }
    }
}
