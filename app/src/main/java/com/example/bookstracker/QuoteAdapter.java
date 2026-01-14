package com.example.bookstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder> {
    private List<Quote> quotes = new ArrayList<>();

    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        Quote current = quotes.get(position);
        holder.tvText.setText("\"" + current.getText() + "\"");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(current.getTimestamp())));
    }

    @Override
    public int getItemCount() { return quotes.size(); }

    public Quote getQuoteAt(int position) {
        return quotes.get(position);
    }

    class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvDate;
        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvQuoteText);
            tvDate = itemView.findViewById(R.id.tvQuoteDate);
        }
    }
}
