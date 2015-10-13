package com.paocorp.joueurdugrenier.youtube;

public class YoutubeChannel {

    protected String channel_id;
    protected String title;
    protected String description;
    protected String thumbnailURL;

    public YoutubeChannel() {
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getChannel_id() {
        return channel_id;
    }

}
