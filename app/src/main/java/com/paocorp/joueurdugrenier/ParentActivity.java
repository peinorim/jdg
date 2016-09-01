package com.paocorp.joueurdugrenier;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.paocorp.joueurdugrenier.models.VideosListAdapter;
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
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
    }

    protected SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd MMMM yyyy à HH'h'mm", Locale.getDefault());
    }

    public ArrayList<YoutubeVideo> customLoadMoreDataFromApi(String offset, String keyword) {
        YoutubeConnector yc = new YoutubeConnector(getBaseContext(), channel_id, null);
        return yc.loadMore(offset, keyword, 10);
    }

    protected void setupList(ListView listview, final String nnextPageToken, final String keyword, final ArrayList<YoutubeVideo> results, final VideosListAdapter adapter, final int ppreLast) {

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            public int preLast = ppreLast;
            public String nextPageToken = nnextPageToken;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount && (totalItemCount % 10) == 0) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        preLast = lastItem;
                    } else {
                        ArrayList<YoutubeVideo> more = customLoadMoreDataFromApi(nextPageToken, keyword);
                        nextPageToken = more.get(more.size() - 1).getNextPageToken();
                        for (int i = 0; i < more.size(); i++) {
                            adapter.add(more.get(i));
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
}
