package com.paocorp.joueurdugrenier.twitter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.paocorp.joueurdugrenier.MainActivity;
import com.paocorp.joueurdugrenier.R;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TwitterActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    /* Shared preference keys */
    private static final String PREF_NAME = "sample_twitter_pref";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    private static final String PREF_USER_NAME = "twitter_user_name";

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 100;

    private ProgressDialog pDialog;

    private static Twitter twitter;
    private static RequestToken requestToken;

    private static SharedPreferences mSharedPreferences;

    private EditText mShareEditText;
    private TextView userName;
    private View loginLayout;
    private View shareLayout;
    ProgressDialog dialog;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;
    private ArrayAdapter<Status> adapter;
    private ListView feed;

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
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        shareLayout = (RelativeLayout) findViewById(R.id.feed_layout);
        //mShareEditText = (EditText) findViewById(R.id.share_text);
        //userName = (TextView) findViewById(R.id.user_name);

		/* register button click listeners */
        findViewById(R.id.btn_login).setOnClickListener(this);
        //findViewById(R.id.btn_share).setOnClickListener(this);

		/* Check if required twitter keys are set */
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key and secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

		/* Initialize application preferences */
        mSharedPreferences = getSharedPreferences(PREF_NAME, 0);

        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

		/*  if already logged in, then hide login layout and show share layout */
        if (true) {
            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);

           // String username = mSharedPreferences.getString(PREF_USER_NAME, "");
           // userName.setText(getResources().getString(R.string.hello) + username);

            Twitter twitter = new TwitterFactory(cb.build()).getInstance();
            Paging paging = new Paging(1, 100);
            try {
                ArrayList<Status> statuses = (ArrayList<Status>) twitter.getUserTimeline(this.getResources().getString(R.string.twitter_jdg_id), paging);
                feed = (ListView) findViewById(R.id.jdg_feed);
                updateTwitterFeed(statuses);

                LinearLayout ly = (LinearLayout) findViewById(R.id.headerLinearLay);
                ly.setBackgroundResource(R.drawable.side_nav_bar_twitter);

                Status sta = statuses.get(0);
                ImageView img1 = (ImageView) findViewById(R.id.channel_img);
                Picasso.with(this).load(sta.getUser().getOriginalProfileImageURL()).into(img1);
                TextView tv = (TextView) findViewById(R.id.channel_desc);
                tv.setText(sta.getUser().getDescription());
            } catch (TwitterException e) {
                e.printStackTrace();
            }

        } else {
            loginLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);

            Uri uri = getIntent().getData();

            if (uri != null && uri.toString().startsWith(callbackUrl)) {

                String verifier = uri.getQueryParameter(oAuthVerifier);

                try {

					/* Getting oAuth authentication token */
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					/* Getting user id form access token */
                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    final String username = user.getName();

					/* save updated token */
                    saveTwitterInfo(accessToken);

                    loginLayout.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    Log.e("Failed to login Twitter!!", e.getMessage());
                }
            }

        }
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
                date.setText(new SimpleDateFormat("dd MMMM - H:mm").format(sta.getCreatedAt()));

                TextView msg = (TextView) convertView.findViewById(R.id.tweet_msg);
                msg.setText(sta.getText());

                return convertView;
            }
        };

        feed.setAdapter(adapter);
    }


    /**
     * Saving user information, after user is authenticated for the first time.
     * You don't need to show user to login, until user has a valid access toen
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userID = accessToken.getUserId();

        User user;
        try {
            user = twitter.showUser(userID);

            String username = user.getName();

			/* Storing oAuth tokens to shared preferences */
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }


    private void loginToTwitter() {
        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                /**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  */
                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {

            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();

                saveTwitterInfo(accessToken);

                loginLayout.setVisibility(View.GONE);
                shareLayout.setVisibility(View.VISIBLE);
                userName.setText(TwitterActivity.this.getResources().getString(R.string.hello) + username);

            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                loginToTwitter();
                break;
/*            case R.id.btn_share:
                final String status = mShareEditText.getText().toString();

                if (status.trim().length() > 0) {
                    new updateTwitterStatus().execute(status);
                } else {
                    Toast.makeText(this, "Message is empty!!", Toast.LENGTH_SHORT).show();
                }
                break;*/
        }
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
        Intent intent = new Intent(TwitterActivity.this, MainActivity.class);
        Bundle b = new Bundle();

        if (id == R.id.channel_jdg) {
            b.putString("channel_id", getResources().getString(R.string.channel_jdg_id));
            intent.putExtras(b);
        } else if (id == R.id.channel_bazar) {
            b.putString("channel_id", getResources().getString(R.string.channel_bazar_id));
            intent.putExtras(b);
        } else if (id == R.id.site_jdg) {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, getResources().getString(R.string.site_jdg_url));
            startActivity(intent);
        } else if (id == R.id.site_aventures) {
            intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, getResources().getString(R.string.site_aventures_url));
            startActivity(intent);

        } else if (id == R.id.nav_fb_jdg) {

        } else if (id == R.id.nav_tw_jdg) {
            intent = new Intent(this, TwitterActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_fb_aventures) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        startActivity(intent);
        dialog.hide();
        return true;
    }

    class updateTwitterStatus extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(TwitterActivity.this);
            pDialog.setMessage("Posting to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected Void doInBackground(String... args) {

            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(consumerKey);
                builder.setOAuthConsumerSecret(consumerSecret);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                StatusUpdate statusUpdate = new StatusUpdate(status);
                InputStream is = getResources().openRawResource(R.raw.jdg);
                statusUpdate.setMedia("test.jpg", is);

                twitter4j.Status response = twitter.updateStatus(statusUpdate);

                Log.d("Status", response.getText());

            } catch (TwitterException e) {
                Log.d("Failed to post!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

			/* Dismiss the progress dialog after sharing */
            pDialog.dismiss();

            Toast.makeText(TwitterActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT).show();

            // Clearing EditText field
            mShareEditText.setText("");
        }

    }

}
