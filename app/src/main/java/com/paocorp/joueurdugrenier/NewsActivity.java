package com.paocorp.joueurdugrenier;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.paocorp.joueurdugrenier.models.NewsConnector;
import com.paocorp.joueurdugrenier.models.NewsItem;
import com.paocorp.joueurdugrenier.models.NewsListAdapter;
import com.paocorp.joueurdugrenier.twitter.TwitterActivity;
import com.paocorp.joueurdugrenier.twitter.WebViewActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsActivity extends ParentActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    PackageInfo pInfo;
    NewsConnector nc;
    NewsListAdapter adapter;
    private ListView listViewNews;
    private ArrayList<NewsItem> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String channel_id = getResources().getString(R.string.channel_jdg_id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.news).setChecked(true);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            if (isNetworkAvailable()) {

                findViewById(R.id.content_offline).setVisibility(View.GONE);

                yc = new YoutubeConnector(this, channel_id, null);

                setTitle(getResources().getString(R.string.title_activity_news));

                LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);

                CircleImageView img1 = (CircleImageView) findViewById(R.id.channel_img);
                Picasso.with(this).load(yc.getChannel().getThumbnailURL()).noFade().into(img1);
                TextView tv = (TextView) findViewById(R.id.channel_desc);
                tv.setText(yc.getChannel().getDescription());

                nc = new NewsConnector(getBaseContext());
                listItems = nc.getItemsList();
                if(listItems.size() > 0) {
                    listViewNews = (ListView) findViewById(R.id.list_view_news);
                    adapter = new NewsListAdapter(getBaseContext(), R.layout.list_item_news, listItems);
                    listViewNews.setAdapter(adapter);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (isNetworkAvailable()) {
            int id = item.getItemId();
            Intent intent = new Intent(this, NewsActivity.class);

            if (id == R.id.channel_jdg) {
                intent = new Intent(this, JDGActivity.class);
            } else if (id == R.id.channel_bazar) {
                intent = new Intent(this, BazarActivity.class);
            } else if (id == R.id.news) {
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
            } else if (id == R.id.nav_rate) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.store_url)));
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
