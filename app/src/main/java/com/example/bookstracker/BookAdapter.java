package com.example.bookstracker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Book> books = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private boolean isRecommendationMode = false; // Controlul modului

    public void setRecommendationMode(boolean mode) {
        this.isRecommendationMode = mode;
    }

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isRecommendationMode) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendations, parent, false);
            return new RecommendationHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
            return new LibraryHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Book currentBook = books.get(position);

        if (holder instanceof LibraryHolder) {
            LibraryHolder lHolder = (LibraryHolder) holder;
            lHolder.textTitle.setText(currentBook.getTitle());
            lHolder.textAuthor.setText(currentBook.getAuthor());
            lHolder.textStatus.setText(currentBook.getStatus());

            if ("Finalizată".equals(currentBook.getStatus())) {
                lHolder.layoutReviewDetails.setVisibility(View.VISIBLE);
                lHolder.cardRatingBar.setRating(currentBook.getRating());

                if (currentBook.getReview() != null && !currentBook.getReview().isEmpty()) {
                    lHolder.textCardReview.setText("\"" + currentBook.getReview() + "\"");
                    lHolder.textCardReview.setVisibility(View.VISIBLE);
                } else {
                    lHolder.textCardReview.setVisibility(View.GONE);
                }
            } else {

                lHolder.layoutReviewDetails.setVisibility(View.GONE);
            }

            lHolder.btnQuoteItem.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
                intent.putExtra("BOOK_ID", currentBook.getId());
                intent.putExtra("OPEN_MIC", true);
                v.getContext().startActivity(intent);
            });

            if (currentBook.getStreak() > 0 && "În curs".equals(currentBook.getStatus())) {
                lHolder.textStreak.setVisibility(View.VISIBLE);
                lHolder.textStreak.setText(String.valueOf(currentBook.getStreak()));
            } else {
                lHolder.textStreak.setVisibility(View.GONE);
            }

            if (currentBook.getPageCount() > 0) {
                int progress = (int) ((currentBook.getPagesRead() * 100L) / currentBook.getPageCount());
                lHolder.progressBar.setProgress(progress);
                lHolder.textPercent.setText(progress + "%");
            } else {
                lHolder.progressBar.setProgress(0);
                lHolder.textPercent.setText("0%");
            }

        } else if (holder instanceof RecommendationHolder) {
            RecommendationHolder rHolder = (RecommendationHolder) holder;
            rHolder.textTitle.setText(currentBook.getTitle());
            rHolder.textAuthor.setText(currentBook.getAuthor());
            rHolder.textRating.setText(String.valueOf(currentBook.getAverageRating()));
            rHolder.textVotes.setText("(" + currentBook.getRatingsCount() + ")");

            String imageUrl = currentBook.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String secureUrl = imageUrl.replace("http://", "https://");
                Glide.with(rHolder.imageCover.getContext())
                        .load(secureUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(rHolder.imageCover);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
            intent.putExtra("BOOK_ID", currentBook.getId());
            intent.putExtra("BOOK_TITLE", currentBook.getTitle());
            intent.putExtra("BOOK_AUTHOR", currentBook.getAuthor());
            intent.putExtra("BOOK_STATUS", currentBook.getStatus());
            intent.putExtra("BOOK_IMAGE", currentBook.getImageUrl());
            v.getContext().startActivity(intent);

            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(currentBook);
            }
        });
    }

    @Override
    public int getItemCount() { return books.size(); }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public Book getBookAt(int position) { return books.get(position); }

    static class LibraryHolder extends RecyclerView.ViewHolder {
         View layoutReviewDetails;
         RatingBar cardRatingBar;
         TextView textCardReview;
        TextView textTitle, textAuthor, textStatus, textStreak, textPercent;
        android.widget.ProgressBar progressBar;
        android.widget.ImageButton btnQuoteItem;
        public LibraryHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textStatus = itemView.findViewById(R.id.textStatus);
            textStreak = itemView.findViewById(R.id.textStreak);
            progressBar = itemView.findViewById(R.id.progressBar);
            textPercent = itemView.findViewById(R.id.textProgressPercent);
            btnQuoteItem = itemView.findViewById(R.id.btnAddQuote);
            textCardReview = itemView.findViewById(R.id.textCardReview);
            cardRatingBar = itemView.findViewById(R.id.cardRatingBar);
            layoutReviewDetails = itemView.findViewById(R.id.layoutReviewDetails);
        }
    }

    static class RecommendationHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textAuthor, textRating, textVotes;
        ImageView imageCover;

        public RecommendationHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitleRec);
            textAuthor = itemView.findViewById(R.id.textAuthorRec);
            textRating = itemView.findViewById(R.id.textRatingRec);
            textVotes = itemView.findViewById(R.id.textVotesRec);
            imageCover = itemView.findViewById(R.id.imageCoverRec);
        }
    }
}