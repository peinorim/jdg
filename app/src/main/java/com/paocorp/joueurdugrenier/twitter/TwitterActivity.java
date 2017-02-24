package com.paocorp.joueurdugrenier.twitter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import com.paocorp.joueurdugrenier.activities.BazarActivity;
import com.paocorp.joueurdugrenier.activities.JDGActivity;
import com.paocorp.joueurdugrenier.activities.NewsActivity;
import com.paocorp.joueurdugrenier.activities.ParentActivity;
import com.paocorp.joueurdugrenier.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class TwitterActivity extends ParentActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 100;

    private ProgressDialog pDialog;

    private static Twitter twitter;
    private static RequestToken requestToken;

    private View shareLayout;
    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;
    private ArrayAdapter<Status> adapter;
    private ListView feed;
    String jdg_id;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/* initializing twitter parameters from string.xml */
        initTwitterConfigs();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken("")
                .setOAuthAccessTokenSecret("");

		/* Enabling strict mode */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

		/* Setting activity layout file */
        setContentView(R.layout.activity_main_twitter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_tw_jdg).setChecked(true);

        shareLayout = (RelativeLayout) findViewById(R.id.feed_layout);

		/* Check if required twitter keys are set */
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key and secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        final Paging paging = new Paging(1, 100);
        try {
            jdg_id = getResources().getString(R.string.twitter_jdg_id);
            ArrayList<Status> statuses = (ArrayList<Status>) twitter.getUserTimeline(this.getResources().getString(R.string.twitter_jdg_id), paging);
            feed = (ListView) findViewById(R.id.jdg_feed);
            updateTwitterFeed(statuses);

            LayoutInflater.from(this).inflate(R.layout.nav_header_main, navigationView);

            LinearLayout ly = (LinearLayout) findViewById(R.id.headerLinearLay);
            ly.setBackgroundResource(R.drawable.side_nav_bar_twitter);

            Status sta = statuses.get(0);

            CircleImageView img1 = (CircleImageView) findViewById(R.id.channel_img);
            Picasso.with(this).load(sta.getUser().getOriginalProfileImageURL()).into(img1);

            TextView tv = (TextView) findViewById(R.id.channel_desc);
            tv.setText(sta.getUser().getDescription());

            TextView tv3 = (TextView) findViewById(R.id.channel_videoCount);
            tv3.setText("@" + sta.getUser().getScreenName());

            TextView tv2 = (TextView) findViewById(R.id.channel_viewCount);
            tv2.setText(getResources().getString(R.string.followers_count, String.valueOf(NumberFormat.getInstance().format(sta.getUser().getFollowersCount()))));

            this.changeTextViewBackground(false);

        } catch (TwitterException e) {
            e.printStackTrace();
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_twitter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if (isNetworkAvailable()) {
                    ArrayList<Status> statuses = null;
                    try {
                        statuses = (ArrayList<Status>) twitter.getUserTimeline(jdg_id, paging);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    feed = (ListView) findViewById(R.id.jdg_feed);
                    updateTwitterFeed(statuses);
                }
                swipeRefreshLayout.setRefreshing(false);
            }

        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void updateTwitterFeed(final ArrayList<Status> array) {
        adapter = new ArrayAdapter<Status>(this, R.layout.video_item, array) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.tweet_item, parent, false);
                }

                Status sta = array.get(position);

                TextView user = (TextView) convertView.findViewById(R.id.twitter_user);
                user.setText("@" + sta.getUser().getScreenName());

                TextView username = (TextView) convertView.findViewById(R.id.twitter_username);
                username.setText(" - " + sta.getUser().getName());

                TextView date = (TextView) convertView.findViewById(R.id.tweet_date);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM - H:mm", Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getDefault());
                date.setText(dateFormat.format(sta.getCreatedAt()));

                TextView msg = (TextView) convertView.findViewById(R.id.tweet_msg);
                msg.setText(sta.getText());

                return convertView;
            }
        };

        feed.setAdapter(adapter);
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                shareLayout.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (isNetworkAvailable()) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            Intent intent = new Intent(TwitterActivity.this, JDGActivity.class);

            if (id == R.id.channel_jdg) {
                intent = new Intent(this, JDGActivity.class);
            } else if (id == R.id.channel_bazar) {
                intent = new Intent(this, BazarActivity.class);
            } else if (id == R.id.news) {
                intent = new Intent(this, NewsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else if (id == R.id.site_jdg) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.site_jdg_url)));
            } else if (id == R.id.site_aventures) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.site_aventures_url)));
            } else if (id == R.id.nav_fb_jdg) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.fb_jdg_url)));
            } else if (id == R.id.nav_tw_jdg) {
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

    @Override
    public void onClick(View v) {

    }
}
