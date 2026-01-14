package com.example.bookstracker;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String author;
    private String status;
    private int streak;
    private int pageCount;
    private int pagesRead;
    private String publisher;
    private float averageRating;
    private int ratingsCount;
    private String imageUrl;
    private float rating;
    private String review;
    public Book() {}
    public Book(String title, String author, String status, int streak, int pageCount, String publisher, float averageRating, int ratingsCount) {
        this.title = title;
        this.author = author;
        this.status = status;
        this.streak = streak;
        this.pageCount = pageCount;
        //this.pagesRead = pagesRead;
        this.publisher = publisher;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
    }
    @Ignore
    public Book(String title, String author, String imageUrl, String status, int streak, int pageCount, String publisher, float averageRating, int ratingsCount) {
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.status = status;
        this.streak = streak;
        this.pageCount = pageCount;
        this.publisher = publisher;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
        this.pagesRead = 0;
    }

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void incrementStreak() {
        this.streak++;
    }

    public int getPageCount() {
        return pageCount;
    }
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
    public int getPagesRead() {
        return pagesRead;
    }
    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public float getAverageRating() {
        return averageRating;
    }
    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }
    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

}
