package com.example.bookstracker;

public class FriendRequest {
    private String requestId; // ID-ul documentului din Firestore
    private String fromUid;
    private String fromUsername;

    public FriendRequest() {}

    public FriendRequest(String requestId, String fromUid, String fromUsername) {
        this.requestId = requestId;
        this.fromUid = fromUid;
        this.fromUsername = fromUsername;
    }

    public String getRequestId() { return requestId; }
    public String getFromUid() { return fromUid; }
    public String getFromUsername() { return fromUsername; }
}
