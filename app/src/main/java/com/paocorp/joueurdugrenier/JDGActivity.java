package com.paocorp.joueurdugrenier;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.paocorp.joueurdugrenier.models.ShowAdsApplication;
import com.paocorp.joueurdugrenier.slidingtabscolors.SlidingTabsColorsFragment;
import com.paocorp.joueurdugrenier.twitter.TwitterActivity;
import com.paocorp.joueurdugrenier.twitter.WebViewActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class JDGActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private YoutubeConnector yc;
    private ArrayList<YoutubeVideo> lastResults;
    private ArrayList<YoutubeVideo> second;
    private ArrayList<YoutubeVideo> third;
    private InterstitialAd mInterstitialAd;
    ProgressDialog dialog;
    PackageInfo pInfo;

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

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            if (isNetworkAvailable()) {

                findViewById(R.id.content_offline).setVisibility(View.GONE);

                yc = new YoutubeConnector(this, channel_id);

                this.lastResults = searchVideos(yc.getChannel().getChannel_id(), null, 10);
                this.second = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.papy_keyword), 10);
                this.third = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.hs_keyword), 10);

                setTitle(yc.getChannel().getTitle());

                ImageView img1 = (ImageView) findViewById(R.id.channel_img);
                Picasso.with(this).load(yc.getChannel().getThumbnailURL()).into(img1);
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

            }

        }

        if (savedInstanceState == null && isNetworkAvailable()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            SlidingTabsColorsFragment fragment = new SlidingTabsColorsFragment(lastResults, second, third, channel_name);
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
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

    public void refreshApp(View v) {
        Intent intent = new Intent(JDGActivity.this, JDGActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private ArrayList<YoutubeVideo> searchVideos(final String channel_id, final String keywords, final int max) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(this.getResources().getString(R.string.loading));
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        startActivity(intent);
        dialog.hide();
        return true;
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showInterstitial() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
}
