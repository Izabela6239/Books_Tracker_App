package com.example.bookstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FriendBookAdapter extends RecyclerView.Adapter<FriendBookAdapter.ViewHolder> {
    private List<Book> books = new ArrayList<>();

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvStatus.setText(book.getStatus());
        holder.tvStreak.setText(String.valueOf(book.getPagesRead() / 10));
    }

    @Override
    public int getItemCount() { return books.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvStatus, tvStreak;
        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.textTitle);
            tvAuthor = v.findViewById(R.id.textAuthor);
            tvStatus = v.findViewById(R.id.textStatus);
            tvStreak = v.findViewById(R.id.textStreak);
        }
    }
}
