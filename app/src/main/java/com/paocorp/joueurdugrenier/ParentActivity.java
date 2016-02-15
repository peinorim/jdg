package com.paocorp.joueurdugrenier;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public abstract class ParentActivity extends AppCompatActivity {

    protected InterstitialAd mInterstitialAd = new InterstitialAd(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
    }
}
