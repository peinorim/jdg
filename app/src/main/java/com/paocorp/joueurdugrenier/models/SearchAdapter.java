package com.paocorp.joueurdugrenier.models;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paocorp.joueurdugrenier.R;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SearchAdapter  extends BaseAdapter {

    private Context context;
    private List<YoutubeVideo> searchItems;

    public SearchAdapter(Context context, List<YoutubeVideo> navDrawerItems) {
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
        date.setText(getDateFormat().format(yt.getDate()));
        description.setText(yt.getDescription());

        return convertView;
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd MMMM yyyy à HH'h'mm", Locale.getDefault());
    }
}
