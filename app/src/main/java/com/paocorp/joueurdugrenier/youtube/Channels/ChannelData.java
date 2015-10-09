package com.paocorp.joueurdugrenier.youtube.Channels;

import com.paocorp.joueurdugrenier.youtube.YoutubeChannelVideos;

public class ChannelData {

    protected String channel_id;
    protected YoutubeChannelVideos videos;

    public ChannelData() {
    }

    public String getChannel_id() {
        return channel_id;
    }

    public YoutubeChannelVideos getVideos() {
        return videos;
    }
}
