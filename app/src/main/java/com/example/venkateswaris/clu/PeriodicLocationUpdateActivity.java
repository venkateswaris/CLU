package com.example.venkateswaris.clu;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;


public class PeriodicLocationUpdateActivity extends ActionBarActivity {

    private PendingIntent pIntent = null;
    private AlarmManager alarmService = null;
    private String fileName = "runningService";
    private final int requestCode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmService = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        BindOnClickEventToContactButton();
        RefreshScheduledTaskStatusInView();
    }

    private void BindOnClickEventToContactButton() {
        Button contactButton = (Button) findViewById(R.id.contact_button);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, requestCode);
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                String name = getColumnValueFromCursor(c, ContactsContract.Contacts.DISPLAY_NAME);
                EditText contactTextBox = (EditText) findViewById(R.id.contact_textbox);
                contactTextBox.setText(name);
                if (Integer.parseInt(c.getString(
                        c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    Cursor contacts = getContacts(contactId);
                    if(contacts.moveToFirst())
                    {
                        String contactNumber = getColumnValueFromCursor(contacts, ContactsContract.CommonDataKinds.Phone.NUMBER);
                        EditText phoneNumberEditView = (EditText) findViewById(R.id.phone_number_hidden);
                        phoneNumberEditView.setText(contactNumber);
                    }
                }
            }
        }
    }

    private String getColumnValueFromCursor(Cursor c, String columnName) {
        return c.getString(c.getColumnIndexOrThrow(columnName));
    }

    private Cursor getContacts(String contactId) {
        // Run query
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                             ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = '"
                + contactId + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        return new ContextWrapper(this).getContentResolver().query(uri, projection, selection, selectionArgs,
                null);
    }

    private void RefreshScheduledTaskStatusInView() {
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        Button stopTrackButton = (Button) findViewById(R.id.stop);
        TextView runningTaskTextView = (TextView) findViewById(R.id.running_task);
        String scheduledServiceDetails = getScheduledServiceDetail();
        if (scheduledServiceDetails != null) {
            runningTaskTextView.setText(scheduledServiceDetails);
            runningTaskTextView.setVisibility(View.VISIBLE);
            stopTrackButton.setVisibility(View.VISIBLE);
        } else {
            runningTaskTextView.setVisibility(View.INVISIBLE);
            stopTrackButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void startTrack(View view) {
        EditText phoneEditText = (EditText) findViewById(R.id.phone_number_hidden);
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        Integer scheduledHour = timePicker.getCurrentHour();
        Integer scheduledMinute = timePicker.getCurrentMinute();
        int scheduledPeriodInMilliseconds = getInMilliseconds(scheduledHour, scheduledMinute);
        String phoneNumber = phoneEditText.getText().toString();
        schedulePeriodicTask(phoneNumber, scheduledPeriodInMilliseconds);
        logAsServiceRunning(phoneNumber, scheduledHour, scheduledMinute);
        RefreshScheduledTaskStatusInView();
    }

    public void stopPeriodicTask(View view) {
        Log.i("", "Stopped Tracking");
        alarmService.cancel(pIntent);
        removeServiceStatusLog();
        RefreshScheduledTaskStatusInView();
    }

    private void removeServiceStatusLog() {
        File dir = getFilesDir();
        File file = new File(dir, fileName);
        file.delete();
    }

    private String getScheduledServiceDetail() {
        try {
            FileInputStream fileOutputStream = openFileInput(fileName);
            int serviceIdSize = 1024;
            byte[] buffer = new byte[serviceIdSize];
            fileOutputStream.read(buffer, 0, serviceIdSize);
            String runningServiceDetails = new String(buffer, Charset.defaultCharset());
            return runningServiceDetails;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void logAsServiceRunning(String phoneNumber, int scheduledHour, int scheduledMinute) {
        try {
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(("Job is scheduled to send \n message to " + phoneNumber + "\nin the interval of\n" + scheduledHour + ":" + scheduledMinute + " Hour").getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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