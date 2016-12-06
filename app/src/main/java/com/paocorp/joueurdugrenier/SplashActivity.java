package com.paocorp.joueurdugrenier;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.paocorp.joueurdugrenier.services.JDGService;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends ParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * Showing splashscreen while making network calls to download necessary
         * data before launching the app Will use AsyncTask to make http call
         */
        new PrefetchData().execute();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    /**
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {

        private ArrayList<YoutubeVideo> lastResults;
        private ArrayList<YoutubeVideo> second;
        private ArrayList<YoutubeVideo> third;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                String channel_id;

                if (isNetworkAvailable()) {
                    if (getIntent().getBooleanExtra("NOTIF", false)) {
                        channel_id = getResources().getString(R.string.channel_bazar_id);
                        yc = new YoutubeConnector(SplashActivity.this, channel_id, null);

                        this.lastResults = searchVideos(null, 10);
                        this.second = searchVideos(getResources().getString(R.string.aventures_keyword), 10);
                        this.third = searchVideos(getResources().getString(R.string.play_keyword), 10);
                    } else {
                        channel_id = getResources().getString(R.string.channel_jdg_id);
                        yc = new YoutubeConnector(SplashActivity.this, channel_id, null);

                        this.lastResults = searchVideos(null, 10);
                        this.second = searchVideos(getResources().getString(R.string.papy_keyword), 10);
                        this.third = searchVideos(getResources().getString(R.string.hs_keyword), 10);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and launch main activity
            Intent intent = new Intent(SplashActivity.this, JDGActivity.class);
            if (getIntent().getStringExtra("NOTIF") == null) {
                intent = new Intent(SplashActivity.this, JDGActivity.class);
            } else if (getIntent().getStringExtra("NOTIF").equals("bazar")) {
                intent = new Intent(SplashActivity.this, BazarActivity.class);
                intent.putExtra("SHOWAD", true);
            } else if (getIntent().getStringExtra("NOTIF").equals("jdg")) {
                intent = new Intent(SplashActivity.this, JDGActivity.class);
                intent.putExtra("SHOWAD", true);
            }

            intent.putParcelableArrayListExtra("lastResults", lastResults);
            intent.putParcelableArrayListExtra("second", second);
            intent.putParcelableArrayListExtra("third", third);

            startActivity(intent);
            // close this activity
            finish();
        }

    }
}
