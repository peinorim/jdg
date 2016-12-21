package com.paocorp.joueurdugrenier.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.paocorp.joueurdugrenier.R;
import com.paocorp.joueurdugrenier.activities.SplashActivity;
import com.paocorp.joueurdugrenier.youtube.YoutubeConnector;
import com.paocorp.joueurdugrenier.youtube.YoutubeVideo;

import java.util.ArrayList;

public class BazarWidgetProvider extends ParentWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        this.context = context.getApplicationContext();
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch SplashActivity
            Intent intent = new Intent(context, SplashActivity.class);
            intent.putExtra("NOTIF", "bazar");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_lastvideo);
            views.setOnClickPendingIntent(R.id.video_thumbnail, pendingIntent);
            views.setOnClickPendingIntent(R.id.video_title, pendingIntent);
            views.setOnClickPendingIntent(R.id.video_description, pendingIntent);

            if (isNetworkAvailable()) {
                YoutubeConnector yc = new YoutubeConnector(context, context.getResources().getString(R.string.channel_bazar_id), null);
                if (yc.getYoutube() != null) {

                    ArrayList<YoutubeVideo> lastResult = yc.search(null, 1);

                    if (lastResult.size() == 1) {
                        YoutubeVideo lastVideo = lastResult.get(0);
                        if (lastVideo != null) {
                            views.setTextViewText(R.id.video_title, lastVideo.getTitle());
                            views.setImageViewBitmap(R.id.video_thumbnail, getBitmapFromURL(lastVideo.getThumbnailURL()));
                            views.setTextViewText(R.id.video_date, getDateFormat().format(lastVideo.getDate()).substring(0, 1).toUpperCase() + getDateFormat().format(lastVideo.getDate()).substring(1));
                            views.setTextViewText(R.id.video_description, lastVideo.getDescription());
                        }
                    }

                    // Tell the AppWidgetManager to perform an update on the current app widget
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        }
    }
}
