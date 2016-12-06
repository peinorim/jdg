package com.paocorp.joueurdugrenier.youtube;

import java.math.BigInteger;

public class YoutubeChannel {

    protected String channel_id;
    protected String title;
    protected String description;
    protected String thumbnailURL;
    protected String bannerURL;
    protected BigInteger viewCount;
    protected BigInteger subscriberCount;
    protected BigInteger videoCount;

    public YoutubeChannel() {
    }

    public String getBannerURL() {
        return bannerURL;
    }

    public void setBannerURL(String bannerURL) {
        this.bannerURL = bannerURL;
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

    public BigInteger getViewCount() {
        return viewCount;
    }

    public void setViewCount(BigInteger viewCount) {
        this.viewCount = viewCount;
    }

    public BigInteger getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(BigInteger subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public BigInteger getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(BigInteger videoCount) {
        this.videoCount = videoCount;
    }
}
