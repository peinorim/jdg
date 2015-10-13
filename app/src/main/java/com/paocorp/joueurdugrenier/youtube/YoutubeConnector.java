package com.paocorp.joueurdugrenier.youtube;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.paocorp.joueurdugrenier.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Channels.List channelQuery;
    private YouTube.Search.List query;
    private String channel_id;
    private String KEY;
    private YoutubeChannel channel = new YoutubeChannel();

    public YoutubeConnector(final Context context, final String channel_id) {

        this.KEY = context.getString(R.string.api_key);
        this.channel_id = channel_id;

        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();
        initChannel();
    }

    public void initChannel(){
        try {
            channelQuery = youtube.channels().list("id,snippet");
            channelQuery.setKey(this.KEY);
            channelQuery.setId(this.channel_id);
            channelQuery.setFields("items(id,snippet/title,snippet/description,snippet/thumbnails)");
        } catch (IOException e) {
            Log.d("YC", "Could not initialize: " + e);
        }

        try {
            ChannelListResponse response = channelQuery.execute();
            List<Channel> results = response.getItems();

            for (Channel result : results) {
                channel.setChannel_id(this.channel_id);
                channel.setTitle(result.getSnippet().getTitle());
                channel.setDescription(result.getSnippet().getDescription());
                channel.setThumbnailURL(result.getSnippet().getThumbnails().getHigh().getUrl());
            }
        } catch (IOException e) {
            Log.d("YC", "Could not search: " + e);
        }
    }

    public ArrayList<YoutubeVideo> search(String keywords) {
        try {
            query = youtube.search().list("id,snippet");
            query.setKey(this.KEY);
            query.setOrder("date");
            query.setType("video");
            if(keywords != null) {
                query.setQ(keywords);
            }
            query.setChannelId(this.channel_id);
            query.setMaxResults((long) 10);
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails)");
        } catch (IOException e) {
            Log.d("YC", "Could not initialize: " + e);
        }
        try {
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            ArrayList<YoutubeVideo> items = new ArrayList<YoutubeVideo>();
            for (SearchResult result : results) {
                YoutubeVideo item = new YoutubeVideo();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                items.add(item);
            }
            return items;
        } catch (IOException e) {
            Log.d("YC", "Could not search: " + e);
            return null;
        }
    }

    public YoutubeChannel getChannel() {
        return channel;
    }
}

