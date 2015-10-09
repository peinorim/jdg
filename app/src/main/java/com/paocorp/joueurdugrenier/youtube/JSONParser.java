package com.paocorp.joueurdugrenier.youtube;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser {

    private String req;
    private JSONObject channeldata;

    public JSONObject getChanneldata() {
        return channeldata;
    }

    public JSONParser(String req) {
        this.req = req;
        this.fetchCHANNELDATA();
    }

    private JSONObject fetchCHANNELDATA() {
        StringBuilder result = new StringBuilder();
        String jsonString = null;
        JSONObject json = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(this.req);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            jsonString = result.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        try {
            json = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.channeldata = json;
        return this.channeldata;
    }
}
