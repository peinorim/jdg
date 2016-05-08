package com.paocorp.joueurdugrenier.youtube;


import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("ParcelCreator")
public class YoutubeVideo implements Parcelable {
    private String id;
    private String channel_id;
    private Date date;
    private String title;
    private String description;
    private String thumbnailURL;
    private String nextPageToken;
    private String keyword;

    public YoutubeVideo(Parcel in) {
        this.id = in.readString();
        this.description = in.readString();
        this.title = in.readString();
        this.thumbnailURL = in.readString();
        try {
            this.date = getDateFormat().parse(in.readString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public YoutubeVideo() {

    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getId() {
        return id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
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

    public String getNextPageToken() {
        return nextPageToken;
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
        dest.writeString(getDateFormat().format(date));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<YoutubeVideo>() {
        public YoutubeVideo createFromParcel(Parcel in) {
            return new YoutubeVideo(in);
        }

        public YoutubeVideo[] newArray(int size) {
            return new YoutubeVideo[size];
        }
    };

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd MMMM yyyy à HH'h'mm", Locale.getDefault());
    }
}
