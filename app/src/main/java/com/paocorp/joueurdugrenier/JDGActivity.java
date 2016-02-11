package com.paocorp.joueurdugrenier;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.paocorp.joueurdugrenier.models.SearchAdapter;
import com.paocorp.joueurdugrenier.models.ShowAdsApplication;
import com.paocorp.joueurdugrenier.slidingtabscolors.SlidingTabsColorsFragment;
import com.paocorp.joueurdugrenier.twitter.TwitterActivity;
import com.paocorp.joueurdugrenier.twitter.WebViewActivity;
import com.paocorp.joueurdugrenier.youtube.PlayerActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class JDGActivity extends ParentActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private YoutubeConnector yc;
    private ArrayList<YoutubeVideo> lastResults;
    private ArrayList<YoutubeVideo> second;
    private ArrayList<YoutubeVideo> third;
    private ListView videosFound;
    PackageInfo pInfo;
    private int preLast;
    private String nextPageToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String channel_id = getResources().getString(R.string.channel_jdg_id);
        String channel_name = getResources().getString(R.string.channel_jdg);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.channel_jdg).setChecked(true);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            if (isNetworkAvailable()) {

                findViewById(R.id.content_offline).setVisibility(View.GONE);

                yc = new YoutubeConnector(this, channel_id);

                this.lastResults = searchVideos(null, 10);
                this.second = searchVideos(getResources().getString(R.string.papy_keyword), 10);
                this.third = searchVideos(getResources().getString(R.string.hs_keyword), 10);

                setTitle(yc.getChannel().getTitle());

                LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);

                CircleImageView img1 = (CircleImageView) findViewById(R.id.channel_img);
                Picasso.with(this).load(yc.getChannel().getThumbnailURL()).noFade().into(img1);
                TextView tv = (TextView) findViewById(R.id.channel_desc);
                tv.setText(yc.getChannel().getDescription());

                ImageView ban = (ImageView) findViewById(R.id.channel_banner);
                Picasso.with(this).load(yc.getChannel().getBannerURL()).into(ban);

                final ShowAdsApplication hideAdObj = ((ShowAdsApplication) getApplicationContext());
                boolean hideAd = hideAdObj.getHideAd();

                if (!hideAd) {
                    mInterstitialAd = new InterstitialAd(this);
                    mInterstitialAd.setAdUnitId(this.getResources().getString(R.string.interstitial));
                    requestNewInterstitial();
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            showInterstitial();
                            hideAdObj.setHideAd(true);
                        }
                    });
                }

                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    TextView txv = (TextView) findViewById(R.id.app_desc);
                    String APPINFO = txv.getText() + " v" + pInfo.versionName;
                    txv.setText(APPINFO);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        if (savedInstanceState == null && isNetworkAvailable()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            SlidingTabsColorsFragment fragment = new SlidingTabsColorsFragment(lastResults, second, third, channel_name);
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    public void refreshApp(View v) {
        if (isNetworkAvailable()) {
            Intent intent = new Intent(JDGActivity.this, JDGActivity.class);
            finish();
            startActivity(intent);
        }
    }


    private ArrayList<YoutubeVideo> searchVideos(final String keywords, final int max) {
        return yc.search(keywords, max);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                if (query.length() >= 3 && isNetworkAvailable()) {
                    findViewById(R.id.sample_content_fragment).setVisibility(View.GONE);
                    findViewById(R.id.channel_banner).setVisibility(View.GONE);
                    findViewById(R.id.videos_search_container).setVisibility(View.VISIBLE);
                    videosFound = (ListView) findViewById(R.id.videos_search_found);
                    final ArrayList<YoutubeVideo> searchResults = searchVideos(query, 15);
                    final SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), R.layout.video_item, searchResults);

                    videosFound.setAdapter(searchAdapter);

                    if (searchAdapter.getCount() > 0) {
                        YoutubeVideo lastItem = (YoutubeVideo) searchAdapter.getItem(searchAdapter.getCount() - 1);
                        nextPageToken = lastItem.getNextPageToken();
                    }

                    videosFound.setOnScrollListener(new AbsListView.OnScrollListener() {

                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {

                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem,
                                             int visibleItemCount, int totalItemCount) {

                            switch (view.getId()) {
                                case R.id.videos_search_found:

                                    final int lastItem = firstVisibleItem + visibleItemCount;
                                    if (lastItem == totalItemCount && (totalItemCount % 15) == 0) {
                                        if (preLast != lastItem) { //to avoid multiple calls for last item
                                            preLast = lastItem;
                                        } else {
                                            if (nextPageToken != null) {
                                                ArrayList<YoutubeVideo> more = yc.loadMore(nextPageToken, query, 15);
                                                if (more.size() > 0) {
                                                    nextPageToken = more.get(more.size() - 1).getNextPageToken();
                                                    for (int i = 0; i < more.size(); i++) {
                                                        searchAdapter.add(more.get(i));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    });

                    videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> av, View v, int pos,
                                                long id) {
                            Intent intent = new Intent(getBaseContext(), PlayerActivity.class);
                            intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                            startActivity(intent);
                        }
                    });
                } else if (query.length() == 0) {
                    findViewById(R.id.videos_search_container).setVisibility(View.GONE);
                    findViewById(R.id.sample_content_fragment).setVisibility(View.VISIBLE);
                    findViewById(R.id.channel_banner).setVisibility(View.VISIBLE);
                }
                return true;
            }

        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (isNetworkAvailable()) {
            int id = item.getItemId();
            Intent intent = new Intent(this, JDGActivity.class);

            if (id == R.id.channel_jdg) {
            } else if (id == R.id.channel_bazar) {
                intent = new Intent(this, BazarActivity.class);
            } else if (id == R.id.site_jdg) {
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, getResources().getString(R.string.site_jdg_url));
            } else if (id == R.id.site_aventures) {
                intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, getResources().getString(R.string.site_aventures_url));
            } else if (id == R.id.nav_fb_jdg) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.fb_jdg_url)));
            } else if (id == R.id.nav_tw_jdg) {
                intent = new Intent(this, TwitterActivity.class);
            } else if (id == R.id.nav_fb_aventures) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.fb_aventures_url)));
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(intent);
        } else {
            loadToast(this.getResources().getString(R.string.offline));
        }
        return true;
    }
}