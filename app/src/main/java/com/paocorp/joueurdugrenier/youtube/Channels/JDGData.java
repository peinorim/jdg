package com.paocorp.joueurdugrenier.youtube.Channels;

import android.content.Context;

import com.paocorp.joueurdugrenier.R;

public class JDGData extends ChannelData {

    public JDGData(Context context) {
        super(context);
        this.channel_id = context.getString(R.string.channel_jdg_id);
    }
}
