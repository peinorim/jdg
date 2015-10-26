package com.paocorp.joueurdugrenier;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paocorp.joueurdugrenier.slidingtabscolors.SlidingTabsColorsFragment;
import com.paocorp.joueurdugrenier.twitter.TwitterActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private YoutubeConnector yc;
    private ArrayList<YoutubeVideo> lastResults;
    private ArrayList<YoutubeVideo> second;
    private ArrayList<YoutubeVideo> third;
    ProgressDialog dialog;
    PackageInfo pInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        String channel_id = getResources().getString(R.string.channel_jdg_id);
        String channel_name = getResources().getString(R.string.channel_jdg);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            b = getIntent().getExtras();
            channel_id = b.getString("channel_id");
            if (channel_id.equals(getResources().getString(R.string.channel_bazar_id))) {
                channel_name = getResources().getString(R.string.channel_bazar);
                super.setTheme(R.style.AppTheme_Bazar_NoActionBar);
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_bazar);
            } else {
                super.setTheme(R.style.AppTheme_NoActionBar);
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
            }
        } else {
            super.setTheme(R.style.AppTheme_NoActionBar);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        }

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
                if (channel_id.equals(getResources().getString(R.string.channel_jdg_id))) {
                    this.second = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.papy_keyword), 10);
                    this.third = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.hs_keyword), 10);
                } else {
                    this.second = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.aventures_keyword), 10);
                    this.third = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.play_keyword), 10);
                    changeTextViewBackground();
                }
                setTitle(yc.getChannel().getTitle());

                ImageView img1 = (ImageView) findViewById(R.id.channel_img);
                Picasso.with(this).load(yc.getChannel().getThumbnailURL()).into(img1);
                TextView tv = (TextView) findViewById(R.id.channel_desc);
                tv.setText(yc.getChannel().getDescription());

                ImageView ban = (ImageView) findViewById(R.id.channel_banner);
                Picasso.with(this).load(yc.getChannel().getBannerURL()).into(ban);

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
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void changeTextViewBackground() {
        LinearLayout ly = (LinearLayout) findViewById(R.id.headerLinearLay);
        ly.setBackgroundResource(R.drawable.side_nav_bar_yellow);
        TextView tv_desc = (TextView) findViewById(R.id.channel_desc);
        tv_desc.setTextColor(this.getResources().getColor(R.color.black));
        TextView tv_app_desc = (TextView) findViewById(R.id.app_desc);
        tv_app_desc.setTextColor(this.getResources().getColor(R.color.black));
        TextView tv_credits = (TextView) findViewById(R.id.credits);
        tv_credits.setTextColor(this.getResources().getColor(R.color.black));
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
        getMenuInflater().inflate(R.menu.main, menu);
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
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        Bundle b = new Bundle();

        if (id == R.id.channel_jdg) {
            b.putString("channel_id", getResources().getString(R.string.channel_jdg_id));
            intent.putExtras(b);
        } else if (id == R.id.channel_bazar) {
            b.putString("channel_id", getResources().getString(R.string.channel_bazar_id));
            intent.putExtras(b);
        } else if (id == R.id.nav_fb) {

        } else if (id == R.id.nav_tw) {
            intent = new Intent(MainActivity.this, TwitterActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        startActivity(intent);
        dialog.hide();
        return true;
    }
}
