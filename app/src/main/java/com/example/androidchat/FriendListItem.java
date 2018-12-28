package com.example.androidchat;

public class FriendListItem {
    private int imageId;
    private String Image;
    private String comment;


    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getText() {
        return comment;
    }
    public void setText(String comment) {
        this.comment = comment;
    }
}