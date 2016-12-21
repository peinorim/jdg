package com.paocorp.joueurdugrenier.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.paocorp.joueurdugrenier.R;
import com.paocorp.joueurdugrenier.activities.SplashActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class JDGService extends IntentService {
    protected YoutubeConnector yc;
    protected YoutubeVideo video;


    public JDGService() {
        super("JDGService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isNetworkAvailable()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean notifJDG = prefs.getBoolean(getResources().getString(R.string.notifJDG), true);
            boolean notifBazar = prefs.getBoolean(getResources().getString(R.string.notifBazar), true);

            if (!notifJDG && !notifBazar) {
                cancelAlarm();
            }

            if (notifJDG) {
                checkChannel(getResources().getString(R.string.channel_jdg_id));
            }
            if (notifBazar) {
                checkChannel(getResources().getString(R.string.channel_bazar_id));
            }
        }
    }

    private void checkChannel(String channelId) {
        yc = new YoutubeConnector(this, channelId, null);
        final ArrayList<YoutubeVideo> lastVideos = yc.getLastVideo();
        video = lastVideos.get(0);
        if (video != null) {
            Date now = new Date();

            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.dateStored), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            try {
                Date lastFetched = getDateFormat().parse(sharedPref.getString(getResources().getString(R.string.dateStored), getDateFormat().format(now)));

                if (lastFetched.before(video.getDate())) {
                    showNotif(video, channelId);
                    editor.putString(getResources().getString(R.string.dateStored), getDateFormat().format(video.getDate()));
                } else {
                    editor.putString(getResources().getString(R.string.dateStored), getDateFormat().format(now));
                }
                editor.apply();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void showNotif(YoutubeVideo video, String channelId) {

        InputStream in;
        Bitmap myBitmap = null;
        try {
            URL url = new URL(video.getThumbnailURL());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
                        .setLargeIcon(myBitmap)
                        .setContentTitle(video.getTitle())
                        .setContentText(video.getDescription().substring(0, 100) + "...");

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, SplashActivity.class);
        if (channelId.equals(getResources().getString(R.string.channel_bazar_id))) {
            resultIntent.putExtra("NOTIF", "bazar");
        } else {
            resultIntent.putExtra("NOTIF", "jdg");
        }

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private SimpleDateFormat getDateFormat() {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return isoFormat;
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), JDGAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, JDGAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}
