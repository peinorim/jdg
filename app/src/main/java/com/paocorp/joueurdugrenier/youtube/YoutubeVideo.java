package com.paocorp.joueurdugrenier.youtube;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;

public class YoutubeVideo implements Parcelable {
    private String id;
    private DateTime date;
    private String title;
    private String description;
    private String thumbnailURL;

    public String getId() {
        return id;
    }

    public DateTime getDate() {
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

    public void setDate(DateTime date) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(thumbnailURL);
        dest.writeValue(date);
    }
}
