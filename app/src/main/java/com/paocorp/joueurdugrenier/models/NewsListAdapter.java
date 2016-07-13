package com.paocorp.joueurdugrenier.models;


import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.paocorp.joueurdugrenier.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NewsListAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<NewsItem> newsItems;

    public NewsListAdapter(Context context, int resource, ArrayList<NewsItem> navDrawerItems) {
        super(context, resource, navDrawerItems);
        this.context = context;
        this.newsItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return newsItems.size();
    }

    @Override
    public Object getItem(int position) {
        return newsItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NewsItem newsItem = newsItems.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.list_item_news, null);

        if(newsItem != null && !newsItem.getTitle().isEmpty()) {
            TextView newstitle = (TextView) convertView.findViewById(R.id.newstitle);
            newstitle.setText(newsItem.getTitle());

            TextView newsdate = (TextView) convertView.findViewById(R.id.newsdate);
            newsdate.setText(getDateFormat().format(newsItem.getDate()));

            TextView newsdesc = (TextView) convertView.findViewById(R.id.newsdesc);
            newsdesc.setText(Html.fromHtml(newsItem.getDesc()).toString().trim());

            TextView newslink = (TextView) convertView.findViewById(R.id.newslink);
            newslink.setText(newsItem.getLink());
        }

        return convertView;
    }

    protected SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("'Publié le' dd MMMM yyyy à HH'h'mm", Locale.getDefault());
    }
}
