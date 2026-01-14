package com.example.bookstracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    void insert(Book book);

    @Query("SELECT * FROM books ORDER BY id DESC")
    List<Book> getAllBooks();

    @Update
    void update(Book book);

    @Delete
    void delete(Book book);

    @Query("SELECT * FROM books WHERE id = :id")
    Book getBookById(int id);

   /* @Query("SELECT * FROM books")
    LiveData<List<Book>> getAllBooks(); //*/
}