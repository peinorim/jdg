package com.paocorp.joueurdugrenier.youtube;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.services.youtube.model.Video;
import com.paocorp.joueurdugrenier.R;

import java.text.NumberFormat;

public class PlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView playerView;
    protected YoutubeConnector yc;
    private Video video;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_player);
        playerView = (YouTubePlayerView) findViewById(R.id.player_view);
        playerView.initialize(getResources().getString(R.string.api_key), this);
        yc = new YoutubeConnector(this, null, getIntent().getStringExtra("VIDEO_ID"));
        video = yc.getVideo();

        String vid_title = getIntent().getStringExtra("VIDEO_TITLE");
        TextView tx_title = (TextView) findViewById(R.id.vid_title);
        tx_title.setText(vid_title);

        String vid_date = getIntent().getStringExtra("VIDEO_DATE");
        TextView tx_date = (TextView) findViewById(R.id.vid_date);
        tx_date.setText(vid_date);

        String vid_desc = getIntent().getStringExtra("VIDEO_DESC");
        TextView tx_desc = (TextView) findViewById(R.id.vid_desc);
        tx_desc.setText(vid_desc);

        if (video != null) {
            String vid_views = getResources().getString(R.string.views, String.valueOf(NumberFormat.getInstance().format(video.getStatistics().getViewCount())));
            TextView tx_views = (TextView) findViewById(R.id.vid_views);
            tx_views.setText(vid_views);
            tx_views.setVisibility(TextView.VISIBLE);
            tx_desc.setText(video.getSnippet().getDescription());
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {
        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean restored) {
        if (!restored) {
            player.cueVideo(getIntent().getStringExtra("VIDEO_ID"));
        }
    }
}
