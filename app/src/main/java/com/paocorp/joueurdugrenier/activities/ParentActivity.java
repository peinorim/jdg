package com.paocorp.joueurdugrenier.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.paocorp.joueurdugrenier.R;
import com.paocorp.joueurdugrenier.models.VideosListAdapter;
import com.paocorp.joueurdugrenier.services.JDGAlarmReceiver;
import com.paocorp.joueurdugrenier.youtube.PlayerActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public abstract class ParentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected InterstitialAd mInterstitialAd = new InterstitialAd(this);
    protected YoutubeConnector yc;
    protected String channel_id;
    protected int preLast;
    protected String nextPageToken;
    protected SwipeRefreshLayout swipeRefreshLayout;

    protected ArrayList<YoutubeVideo> lastResults;
    protected ArrayList<YoutubeVideo> second;
    protected ArrayList<YoutubeVideo> third;
    protected ListView listViewFirst;
    protected ListView listViewSecond;
    protected ListView listViewThird;
    protected VideosListAdapter adapter1;
    protected VideosListAdapter adapter2;
    protected VideosListAdapter adapter3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected ArrayList<YoutubeVideo> searchVideos(final String keywords, final int max) {
        return yc.search(keywords, max);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    protected void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    protected void loadToast(String mess) {
        Context context = getApplicationContext();
        CharSequence text = mess;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    protected void changeTextViewBackground(boolean changeSideBar) {
        if (changeSideBar) {
            LinearLayout ly = (LinearLayout) findViewById(R.id.headerLinearLay);
            ly.setBackgroundResource(R.drawable.side_nav_bar_green);
        }
        TextView tv_desc = (TextView) findViewById(R.id.channel_desc);
        tv_desc.setTextColor(this.getResources().getColor(R.color.white));
        TextView tv_app_desc = (TextView) findViewById(R.id.app_desc);
        tv_app_desc.setTextColor(this.getResources().getColor(R.color.white));
        TextView tv_credits = (TextView) findViewById(R.id.credits);
        tv_credits.setTextColor(this.getResources().getColor(R.color.white));
        tv_credits.setLinkTextColor(this.getResources().getColor(R.color.white));

        TextView videoCount = (TextView) findViewById(R.id.channel_videoCount);
        videoCount.setTextColor(this.getResources().getColor(R.color.white));
        TextView viewCount = (TextView) findViewById(R.id.channel_viewCount);
        viewCount.setTextColor(this.getResources().getColor(R.color.white));
        TextView subsCount = (TextView) findViewById(R.id.channel_subscriberCount);
        subsCount.setTextColor(this.getResources().getColor(R.color.white));
    }

    protected SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("EEEE d MMMM yyyy à HH'h'mm", Locale.getDefault());
    }

    public ArrayList<YoutubeVideo> customLoadMoreDataFromApi(String offset, String keyword) {
        YoutubeConnector yc = new YoutubeConnector(getBaseContext(), channel_id, null);
        return yc.loadMore(offset, keyword, 10);
    }

    protected void setupList(final ListView listview, final String nnextPageToken, final String keyword, final ArrayList<YoutubeVideo> results, final VideosListAdapter adapter, final int ppreLast) {

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            int preLast = ppreLast;
            String nextPageToken = nnextPageToken;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                int topRowVerticalPosition = (listview == null || listview.getChildCount() == 0) ? 0 : listview.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);

                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount && (totalItemCount % 10) == 0) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        preLast = lastItem;
                    } else {
                        ArrayList<YoutubeVideo> more = customLoadMoreDataFromApi(nextPageToken, keyword);
                        if (more.size() > 0) {
                            nextPageToken = more.get(more.size() - 1).getNextPageToken();
                            for (int i = 0; i < more.size(); i++) {
                                adapter.add(more.get(i));
                            }
                        }
                    }
                }
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Intent intent = new Intent(getBaseContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", results.get(pos).getId());
                intent.putExtra("VIDEO_TITLE", results.get(pos).getTitle());
                intent.putExtra("VIDEO_DATE", getDateFormat().format(results.get(pos).getDate()));
                intent.putExtra("VIDEO_DESC", results.get(pos).getDescription());
                startActivity(intent);
            }
        });
    }

    protected void fetchVideos(boolean fromSplash, String keyword1, String keyword2) {

        swipeRefreshLayout.setRefreshing(true);
        if (isNetworkAvailable()) {

            if (!fromSplash) {
                this.lastResults = searchVideos(null, 10);
                this.second = searchVideos(keyword1, 10);
                this.third = searchVideos(keyword2, 10);
            }

            /************************************FIRST LIST****************************************/
            listViewFirst = (ListView) findViewById(R.id.first_list);
            adapter1 = new VideosListAdapter(this, R.layout.video_item, this.lastResults);
            listViewFirst.setAdapter(adapter1);

            if (adapter1.getCount() > 0) {
                YoutubeVideo lastItem = (YoutubeVideo) adapter1.getItem(adapter1.getCount() - 1);
                nextPageToken = lastItem.getNextPageToken();
            }

            setupList(listViewFirst, nextPageToken, null, this.lastResults, adapter1, preLast);

            /***********************************SECOND LIST****************************************/
            listViewSecond = (ListView) findViewById(R.id.second_list);
            adapter2 = new VideosListAdapter(this, R.layout.video_item, this.second);
            listViewSecond.setAdapter(adapter2);

            if (adapter2.getCount() > 0) {
                YoutubeVideo lastItem = (YoutubeVideo) adapter2.getItem(adapter2.getCount() - 1);
                nextPageToken = lastItem.getNextPageToken();
            }

            setupList(listViewSecond, nextPageToken, keyword1, this.second, adapter2, preLast);

            /************************************THIRD LIST****************************************/
            listViewThird = (ListView) findViewById(R.id.third_list);
            adapter3 = new VideosListAdapter(this, R.layout.video_item, this.third);
            listViewThird.setAdapter(adapter3);

            if (adapter3.getCount() > 0) {
                YoutubeVideo lastItem = (YoutubeVideo) adapter3.getItem(adapter3.getCount() - 1);
                nextPageToken = lastItem.getNextPageToken();
            }

            setupList(listViewThird, nextPageToken, keyword2, this.third, adapter3, preLast);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    // Setup a recurring alarm every half hour
    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), JDGAlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 3600 * 1000 * 3, pIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), JDGAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, JDGAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}
