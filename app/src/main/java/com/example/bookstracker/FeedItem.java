package com.example.bookstracker;

public class FeedItem {
    private String userName;
    private String bookTitle;
    private String actionText;
    private float rating;
    private String userImageUrl;

    public FeedItem(String userName, String bookTitle, String actionText, float rating) {
        this.userName = userName;
        this.bookTitle = bookTitle;
        this.actionText = actionText;
        this.rating = rating;
    }

    // Getteri
    public String getUserName() { return userName; }
    public String getBookTitle() { return bookTitle; }
    public String getActionText() { return actionText; }
    public float getRating() { return rating; }
}
