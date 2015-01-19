package com.example.venkateswaris.clu;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class PeriodicLocationUpdate extends ActionBarActivity {

    private PendingIntent pIntent = null;
    private AlarmManager alarmService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmService=  (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        AddScheduledTasksToView(null);
    }

    private void AddScheduledTasksToView(List<String> scheduledTasks) {
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void startTrack(View view){
        EditText phoneEditText = (EditText) findViewById(R.id.phone_text);
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        int scheduledPeriodInMilliseconds = getInMilliseconds(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        schedulePeriodicTask(phoneEditText.getText().toString(), scheduledPeriodInMilliseconds);
    }

    private int getInMilliseconds(Integer currentHour, Integer currentMinute) {
        int totalSeconds = (currentHour * 3600) + (currentMinute * 60);
        return totalSeconds * 1000;
    }

    private void schedulePeriodicTask(String phoneNumber, int intervalMillis) {
        Intent serviceBroadCasterIntent = new Intent(this, CLUServiceBroadcastReceiver.class);
        serviceBroadCasterIntent.putExtra("phoneNumber", phoneNumber);
         pIntent = PendingIntent.getBroadcast(this, CLUServiceBroadcastReceiver.REQUEST_CODE,
                serviceBroadCasterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmService.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pIntent);
    }

    public void stopPeriodicTask(View view){
        Log.i("","Stopped Tracking");
        alarmService.cancel(pIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
