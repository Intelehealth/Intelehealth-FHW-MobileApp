package org.intelehealth.ezazi.ui.rtc.activity;

import android.content.Intent;
import android.os.Bundle;

import org.intelehealth.apprtc.ChatActivity;
import org.intelehealth.apprtc.CompleteActivity;
import org.intelehealth.ezazi.R;

/**
 * Created by Vaghela Mithun R. on 24-05-2023 - 18:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class EzaziChatActivity extends ChatActivity {
    @Override
    protected int getContentResourceId() {
        return R.layout.activity_chat_ezazi;
    }

    @Override
    protected void setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        super.setupActionBar();
    }

    @Override
    protected Intent getVideoIntent() {
        return new Intent(this, VideoCallActivity.class);
    }
}
