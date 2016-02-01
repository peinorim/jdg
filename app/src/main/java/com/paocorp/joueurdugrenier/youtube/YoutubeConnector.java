package com.paocorp.joueurdugrenier.youtube;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Channels.List channelQuery;
    private YouTube.Search.List query;
    private String channel_id;
    private String KEY;
    private Context context;
    private String queryParams = "nextPageToken, items(id/videoId,snippet/title,snippet/description,snippet/thumbnails,snippet/publishedAt)";
    private YoutubeChannel channel = new YoutubeChannel();

    public YoutubeConnector(final Context context, final String channel_id) {

        this.KEY = context.getString(R.string.api_key);
        this.channel_id = channel_id;
        this.context = context;

        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();
        initChannel();
    }

    public void initChannel() {

        ChannelListResponse response = null;
        try {
            channelQuery = youtube.channels().list("id,snippet,brandingSettings");
            channelQuery.setKey(this.KEY);
            channelQuery.setId(this.channel_id);
            channelQuery.setFields("items(id,brandingSettings,snippet/title,snippet/description,snippet/thumbnails)");
        } catch (IOException e) {
            Log.d("YC", "Could not init: " + e);
        }

        try {
            response = channelQuery.execute();
        } catch (GoogleJsonResponseException e) {
            Log.d("YC", "Could not search: " + e.getMessage());
            switch (e.getStatusCode()) {
                case 403:
                    this.KEY = this.context.getString(R.string.api_key2);
                    channelQuery.setKey(this.KEY);
                    try {
                        response = channelQuery.execute();
                    } catch (IOException e1) {
                        Log.d("YC-KEY2", "Could not initialize: " + e1.getMessage());
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Channel> results = response.getItems();

        for (Channel result : results) {
            channel.setChannel_id(this.channel_id);
            channel.setTitle(result.getSnippet().getTitle());
            channel.setDescription(result.getSnippet().getDescription());
            channel.setThumbnailURL(result.getSnippet().getThumbnails().getHigh().getUrl());
            if (isTablet(this.context)) {
                channel.setBannerURL(result.getBrandingSettings().getImage().getBannerTabletImageUrl());
            } else {
                channel.setBannerURL(result.getBrandingSettings().getImage().getBannerImageUrl());
            }
        }
    }

    public ArrayList<YoutubeVideo> search(String keywords, int max) {
        try {
            query = youtube.search().list("id,snippet");
            query.setKey(this.KEY);
            query.setOrder("date");
            query.setType("video");
            if (keywords != null) {
                query.setQ(keywords);
            }
            query.setChannelId(this.channel_id);
            query.setMaxResults((long) max);
            query.setFields(queryParams);
        } catch (IOException e) {
            Log.d("SEARCH", "Could not initialize search: " + e.getMessage());
        }

        SearchListResponse response = null;
        try {
            response = query.execute();

        } catch (GoogleJsonResponseException e) {
            switch (e.getStatusCode()) {
                case 403:
                    this.KEY = this.context.getString(R.string.api_key2);
                    query.setKey(this.KEY);
                    try {
                        response = query.execute();
                    } catch (IOException e1) {
                        Log.d("SEARCH-KEY2", "Could not initialize: " + e1.getMessage());
                    }
            }
        } catch (IOException e) {
            Log.d("YC", "Could not search: " + e.getMessage());
            return null;
        }
        List<SearchResult> results = response.getItems();

        ArrayList<YoutubeVideo> items = new ArrayList<YoutubeVideo>();
        for (SearchResult result : results) {
            YoutubeVideo item = new YoutubeVideo();
            item.setTitle(result.getSnippet().getTitle());
            item.setDescription(result.getSnippet().getDescription());
            item.setThumbnailURL(result.getSnippet().getThumbnails().getMedium().getUrl());
            item.setId(result.getId().getVideoId());
            item.setNextPageToken(response.getNextPageToken());
            if (keywords != null) {
                item.setKeyword(keywords);
            }
            try {
                item.setDate(getDateFormat().parse(result.getSnippet().getPublishedAt().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            item.setChannel_id(this.channel_id);
            items.add(item);
        }
        return items;
    }

    public ArrayList<YoutubeVideo> loadMore(String offset, String keyword) {
        try {
            query = youtube.search().list("id,snippet");
            query.setKey(this.KEY);
            query.setOrder("date");
            query.setType("video");
            query.setChannelId(this.channel_id);
            if (keyword != null) {
                query.setQ(keyword);
            }
            query.setPageToken(offset);
            query.setMaxResults((long) 10);
            query.setFields(queryParams);
        } catch (IOException e) {
            Log.d("MORE", "Could not load more: " + e.getMessage());
        }

        SearchListResponse response = null;
        try {
            response = query.execute();

        } catch (GoogleJsonResponseException e) {
            switch (e.getStatusCode()) {
                case 403:
                    this.KEY = this.context.getString(R.string.api_key2);
                    query.setKey(this.KEY);
                    try {
                        response = query.execute();
                    } catch (IOException e1) {
                        Log.d("MORE-KEY2", "Could not initialize: " + e1.getMessage());
                    }
            }
        } catch (IOException e) {
            Log.d("MORE", "Could not execute more: " + e.getMessage());
            return null;
        }
        List<SearchResult> results = response.getItems();

        ArrayList<YoutubeVideo> items = new ArrayList<YoutubeVideo>();
        for (SearchResult result : results) {
            YoutubeVideo item = new YoutubeVideo();
            item.setTitle(result.getSnippet().getTitle());
            item.setDescription(result.getSnippet().getDescription());
            item.setThumbnailURL(result.getSnippet().getThumbnails().getMedium().getUrl());
            item.setId(result.getId().getVideoId());
            item.setNextPageToken(response.getNextPageToken());
            if (keyword != null) {
                item.setKeyword(keyword);
            }
            try {
                item.setDate(getDateFormat().parse(result.getSnippet().getPublishedAt().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            item.setChannel_id(this.channel_id);
            items.add(item);
        }
        return items;
    }

    public YoutubeChannel getChannel() {
        return channel;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    }
}

