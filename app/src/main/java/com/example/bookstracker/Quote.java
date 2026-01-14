package com.example.bookstracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quotes")
public class Quote {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String bookId;
    public String text;
    public long timestamp;

    public Quote(String bookId, String text, long timestamp) {
        this.bookId = bookId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBookId() { return bookId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
}
