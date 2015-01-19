package com.example.venkateswaris.clu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CLUServiceBroadcastReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1234;

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra("phoneNumber");
        Intent serviceIntent = new Intent(context, LocationUpdateService.class);
        serviceIntent.putExtra("phoneNumber",phoneNumber);
        context.startService(serviceIntent);
    }
}
