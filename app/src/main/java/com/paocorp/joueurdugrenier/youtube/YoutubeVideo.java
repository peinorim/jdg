package com.paocorp.joueurdugrenier.youtube;


import java.util.Date;

public class YoutubeVideo {
    private String id;
    private Date date;
    private String title;
    private String description;
    private String thumbnail;

    public YoutubeVideo(String id, Date date, String title, String description, String thumbnail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
