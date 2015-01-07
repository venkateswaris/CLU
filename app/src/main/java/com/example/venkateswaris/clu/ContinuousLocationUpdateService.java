package com.example.venkateswaris.clu;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class ContinuousLocationUpdateService extends IntentService {

    public ContinuousLocationUpdateService(String name) {
        super(name);
    }

    public ContinuousLocationUpdateService(){
        super("Continuous Location Update Service");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
