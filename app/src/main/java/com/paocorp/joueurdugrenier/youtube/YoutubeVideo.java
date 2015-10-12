package com.paocorp.joueurdugrenier.youtube;


import java.util.Date;

public class YoutubeVideo {
    private String id;
    private Date date;
    private String title;
    private String description;
    private String thumbnailURL;

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
