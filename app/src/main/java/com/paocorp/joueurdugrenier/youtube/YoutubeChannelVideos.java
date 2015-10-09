package com.paocorp.joueurdugrenier.youtube;


import com.paocorp.joueurdugrenier.youtube.Channels.ChannelData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class YoutubeChannelVideos {

    private String url = "https://www.googleapis.com/youtube/v3/search?key=AIzaSyA9lKLIH3Eqb3oZ3qQTCf7LkI_WePcvnyg&channelId=UC_yP2DpIgs5Y1uWC0T03Chw&part=snippet,id&order=date&maxResults=20";
    private String channel_id;
    private String api_key = new APIKey().getApi_key();
    private List<YoutubeVideo> videos_list;


    public YoutubeChannelVideos(ChannelData channel) {
        this.channel_id = channel.getChannel_id();

    }

    public void fetchLastVideos(){
        JSONParser parser = new JSONParser(this.url);
        JSONObject obj = parser.getChanneldata();
        try {
            JSONArray items = obj.getJSONArray("items");
            for(int i = 0 ; i < items.length() ; i++){
                JSONObject snippet = new JSONObject(items.getJSONObject(i).getString("snippet"));

                JSONObject id = new JSONObject(items.getJSONObject(i).getString("id"));
                String videoId = id.getString("videoId");

                String title = snippet.getString("title");
                String date = snippet.getString("publishedAt");
                String description = snippet.getString("description");

                JSONObject thumbs = new JSONObject(snippet.getString("thumbnails"));
                JSONObject high = new JSONObject(thumbs.getString("default"));
                String thumb_high = high.getString("url");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public List<YoutubeVideo> getVideos_list() {
        return videos_list;
    }
}
