package org.intelehealth.ezazi.services.firebase_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = RestartServiceReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
//        if (!CallListenerBackgroundService.isInstanceCreated()) {
//            Intent serviceIntent = new Intent(context, CallListenerBackgroundService.class);
//            context.startService(serviceIntent);
//        }
    }
}