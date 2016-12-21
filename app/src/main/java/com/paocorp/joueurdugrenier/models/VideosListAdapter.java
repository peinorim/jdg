package com.paocorp.joueurdugrenier.models;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paocorp.joueurdugrenier.R;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VideosListAdapter extends ArrayAdapter {

    private Context context;
    private List<YoutubeVideo> searchItems;

    public VideosListAdapter(Context context, int resource, List<YoutubeVideo> navDrawerItems) {
        super(context, resource, navDrawerItems);
        this.context = context;
        this.searchItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return searchItems.size();
    }

    @Override
    public Object getItem(int position) {
        return searchItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        YoutubeVideo yt = searchItems.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.video_item, null);

        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.video_title);
        TextView date = (TextView) convertView.findViewById(R.id.video_date);
        TextView description = (TextView) convertView.findViewById(R.id.video_description);

        Picasso.with(context).load(yt.getThumbnailURL()).into(thumbnail);
        title.setText(yt.getTitle());
        String sDate = getDateFormat().format(yt.getDate()).substring(0, 1).toUpperCase() + getDateFormat().format(yt.getDate()).substring(1);
        date.setText(sDate);
        description.setText(yt.getDescription());

        return convertView;
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("EEEE d MMMM yyyy à HH'h'mm", Locale.getDefault());
    }
}
