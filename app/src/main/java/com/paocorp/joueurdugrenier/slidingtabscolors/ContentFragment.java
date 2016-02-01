/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paocorp.joueurdugrenier.slidingtabscolors;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.paocorp.joueurdugrenier.youtube.PlayerActivity;
import com.paocorp.joueurdugrenier.R;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simple Fragment used to display some meaningful content for each page in the sample's
 * {@link android.support.v4.view.ViewPager}.
 */
public class ContentFragment extends Fragment implements AbsListView.OnScrollListener {

    private static final String KEY_TITLE = "title";
    private static final String KEY_INDICATOR_COLOR = "indicator_color";
    private static final String KEY_DIVIDER_COLOR = "divider_color";
    private static final String LAST_RESULTS = "last_results";
    private ArrayList<YoutubeVideo> searchResults;
    private ArrayAdapter<YoutubeVideo> adapter;
    private ListView videosFound;
    private String channel_id;
    private String nextPageToken;
    private String keyword;
    private int preLast;

    /**
     * @return a new instance of {@link ContentFragment}, adding the parameters into a bundle and
     * setting them as arguments.
     */
    public static ContentFragment newInstance(CharSequence title, int indicatorColor,
                                              int dividerColor, List<YoutubeVideo> mLastResults) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(KEY_TITLE, title);
        bundle.putInt(KEY_INDICATOR_COLOR, indicatorColor);
        bundle.putInt(KEY_DIVIDER_COLOR, dividerColor);
        bundle.putParcelableArrayList(LAST_RESULTS, (ArrayList<? extends Parcelable>) mLastResults);

        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            this.searchResults = args.getParcelableArrayList(LAST_RESULTS);
            videosFound = (ListView) view.findViewById(R.id.videos_found);
            updateVideosFound(searchResults);
            addClickListener();
            videosFound.setOnScrollListener(this);
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView lw, final int firstVisibleItem,
                         final int visibleItemCount, final int totalItemCount) {

        switch (lw.getId()) {
            case R.id.videos_found:

                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        preLast = lastItem;
                    } else {
                        if (nextPageToken != null) {
                            ArrayList<YoutubeVideo> more = customLoadMoreDataFromApi(nextPageToken, keyword);
                            nextPageToken = more.get(more.size() - 1).getNextPageToken();
                            for (int i = 0; i < more.size(); i++) {
                                adapter.add(more.get(i));
                            }
                        }
                    }
                }
        }
    }

    public ArrayList<YoutubeVideo> customLoadMoreDataFromApi(String offset, String keyword) {
        YoutubeConnector yc = new YoutubeConnector(this.getContext(), channel_id);
        return yc.loadMore(offset, keyword);
    }

    private void updateVideosFound(final ArrayList<YoutubeVideo> ay) {
        adapter = new ArrayAdapter<YoutubeVideo>(getContext(), R.layout.video_item, ay) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.video_title);
                TextView date = (TextView) convertView.findViewById(R.id.video_date);
                TextView description = (TextView) convertView.findViewById(R.id.video_description);

                YoutubeVideo searchResult = ay.get(position);
                nextPageToken = searchResult.getNextPageToken();
                keyword = searchResult.getKeyword();
                channel_id = searchResult.getChannel_id();

                Picasso.with(getContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                date.setText(getDateFormat().format(searchResult.getDate()));
                description.setText(searchResult.getDescription());
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void addClickListener() {
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                startActivity(intent);
            }

        });
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd MMMM yyyy à HH'h'mm", Locale.getDefault());
    }
}
