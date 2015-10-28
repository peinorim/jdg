package com.paocorp.joueurdugrenier.twitter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.paocorp.joueurdugrenier.R;

public class WebViewActivity extends Activity {

    private WebView webView;

    public static String EXTRA_URL = "extra_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.twitter_webview);

        setTitle("Login");

        final String url = this.getIntent().getStringExtra(EXTRA_URL);
        if (null == url) {
            Log.e("Twitter", "URL cannot be null");
            finish();
        }

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(url);
    }


    class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.contains(getResources().getString(R.string.twitter_callback))) {
                Uri uri = Uri.parse(url);

				/* Sending results back */
                String verifier = uri.getQueryParameter(getString(R.string.twitter_oauth_verifier));
                Intent resultIntent = new Intent();
                resultIntent.putExtra(getString(R.string.twitter_oauth_verifier), verifier);
                setResult(RESULT_OK, resultIntent);

				/* closing webview */
                finish();
                return true;
            }
            return false;
        }
    }
}
