package com.paocorp.joueurdugrenier;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;


import com.paocorp.joueurdugrenier.slidingtabscolors.SlidingTabsColorsFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        String channel_name = getResources().getString(R.string.channel_jdg);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String channel_id;
            Bundle b = getIntent().getExtras();
            if(b != null) {
                b = getIntent().getExtras();
                channel_id = b.getString("channel_id");
            } else {
                channel_id = getResources().getString(R.string.channel_jdg_id);
                channel_name = getResources().getString(R.string.channel_bazar);
            }

            yc = new YoutubeConnector(this, channel_id);

            this.lastResults = searchVideos(yc.getChannel().getChannel_id(), null);
            if(channel_id == getResources().getString(R.string.channel_jdg_id)) {
                this.second = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.papy_keyword));
                this.third = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.hs_keyword));
            } else {
                this.second = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.aventures_keyword));
                this.third = searchVideos(yc.getChannel().getChannel_id(), getResources().getString(R.string.play_keyword));
            }
            setTitle(yc.getChannel().getTitle());

            ImageView img1 = (ImageView) findViewById(R.id.imageView);
            Picasso.with(this).load(yc.getChannel().getThumbnailURL()).into(img1);
            TextView tv = (TextView) findViewById(R.id.channel_desc);
            tv.setText(yc.getChannel().getDescription());
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            SlidingTabsColorsFragment fragment = new SlidingTabsColorsFragment(lastResults, second, third, channel_name);
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    private ArrayList<YoutubeVideo> searchVideos(final String channel_id, final String keywords) {
        yc = new YoutubeConnector(this, channel_id);
        return yc.search(keywords);
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
        int id = item.getItemId();
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        Bundle b = new Bundle();

        if (id == R.id.channel_jdg) {
            b.putString("channel_id", getResources().getString(R.string.channel_jdg_id));
        } else if (id == R.id.channel_bazar) {
            b.putString("channel_id", getResources().getString(R.string.channel_bazar_id));
        } else if (id == R.id.nav_fb) {

        } else if (id == R.id.nav_tw) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        intent.putExtras(b);
        startActivity(intent);
        return true;
    }
}
