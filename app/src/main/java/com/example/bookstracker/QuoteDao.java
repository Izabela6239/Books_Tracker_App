package com.example.bookstracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuoteDao {
    @Insert
    void insert(Quote quote);

    @Query("SELECT * FROM quotes WHERE bookId = :bookId")
    List<Quote> getQuotesForBook(String bookId);

    @Delete
    void delete(Quote quote);
}
