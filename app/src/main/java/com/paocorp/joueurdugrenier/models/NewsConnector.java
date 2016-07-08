package com.paocorp.joueurdugrenier.models;

import android.content.Context;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;
import com.paocorp.joueurdugrenier.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.DataFormatException;

public class NewsConnector {
    private Context context;
    private String desc;
    private Date date;
    private ArrayList<NewsItem> itemsList;

    public NewsConnector(Context ctx) {
        this.context = ctx;
        //https://github.com/einmalfel/Earl
        InputStream inputStream = null;
        try {
            inputStream = new URL(this.context.getString(R.string.feed)).openConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(inputStream != null) {
                Feed feed = EarlParser.parseOrThrow(inputStream, 0);
                this.desc = feed.getDescription();
                this.date = feed.getPublicationDate();
                itemsList = new ArrayList<NewsItem>();

                for (Item item : feed.getItems()) {
                    NewsItem newsItem = new NewsItem();
                    newsItem.setTitle(item.getTitle());
                    newsItem.setDesc(item.getDescription());
                    newsItem.setDate(item.getPublicationDate());
                    newsItem.setLink(item.getLink());
                    itemsList.add(newsItem);
                }
            }
        } catch (XmlPullParserException | IOException | DataFormatException e) {
            e.printStackTrace();
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<NewsItem> getItemsList() {
        return itemsList;
    }

    public void setItemsList(ArrayList<NewsItem> itemsList) {
        this.itemsList = itemsList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
