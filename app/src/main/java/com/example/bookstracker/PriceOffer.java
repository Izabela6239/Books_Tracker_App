package com.example.bookstracker;

public class PriceOffer {
    private String storeName;
    private String price;
    private String link;

    public PriceOffer(String storeName, String price, String link) {
        this.storeName = storeName;
        this.price = price;
        this.link = link;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getPrice() {
        return price;
    }

    public String getLink() {
        return link;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
