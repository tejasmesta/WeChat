package com.example.wechat.findfriends;

public class FindFriendsModelClass {
    private String username;
    private String photo;
    private String userId;
    private Boolean requestSent;

    public FindFriendsModelClass(String username, String photo, String userId, Boolean requestSent) {
        this.username = username;
        this.photo = photo;
        this.userId = userId;
        this.requestSent = requestSent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getRequestSent() {
        return requestSent;
    }

    public void setRequestSent(Boolean requestSent) {
        this.requestSent = requestSent;
    }
}
