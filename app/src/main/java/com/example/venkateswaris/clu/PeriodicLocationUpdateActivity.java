package com.example.venkateswaris.clu;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class PeriodicLocationUpdateActivity extends ActionBarActivity {

    private PendingIntent pIntent = null;
    private AlarmManager alarmService = null;
    private String fileName = "runningService";
    private final int requestCode = 1;
    private ArrayList<String> phoneNumberList = new ArrayList<>();
    private int scheduledHour = 0;
    private int scheduledMin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmService = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        BindOnClickEventToContactButton();
        BindOnClickEventToClearContactButton();
        BindOnClickEventToClearTimeButton();
        BindOnClickEventToSetRepeatTimeButton();
        RefreshScheduledTaskStatusInView();
    }

    private void BindOnClickEventToClearTimeButton() {
        Button timeButton = (Button) findViewById(R.id.clear_time);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView timeTextBox = (TextView) findViewById(R.id.repeat_time_textbox);
                timeTextBox.setText(null);
                scheduledMin = 0;
                scheduledHour = 0;
            }
        });
    }

    private void BindOnClickEventToSetRepeatTimeButton() {
        final Context context = this;
        Button button = (Button) findViewById(R.id.Repeat_Time);
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener callback = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        TextView repeatTimeTextBox = (TextView) findViewById(R.id.repeat_time_textbox);
                        repeatTimeTextBox.setText(hourOfDay + ":" + minute +"hr");
                        scheduledHour = hourOfDay;
                        scheduledMin = minute;
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, callback, 24, 60, true);
                timePickerDialog.setTitle("Set Repeat Time");
                timePickerDialog.show();
            }
        });
    }

    private void BindOnClickEventToClearContactButton() {
        Button contactButton = (Button) findViewById(R.id.clear_contact);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView contactTextBox = (TextView) findViewById(R.id.contact_text_box);
                contactTextBox.setText(null);
                phoneNumberList = new ArrayList<String>();
            }
        });
    }

    private void BindOnClickEventToContactButton() {
        ImageButton contactButton = (ImageButton) findViewById(R.id.contact_image_button);
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
                EditText contactTextBox = (EditText) findViewById(R.id.contact_text_box);
                    Editable contactText = contactTextBox.getText();
                if(contactText.length()!=0) {
                    contactText.append("\n");
                }
                contactText.append(name);
                if (Integer.parseInt(c.getString(
                        c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    Cursor contacts = getContacts(contactId);
                    if(contacts.moveToFirst())
                    {
                        String contactNumber = getColumnValueFromCursor(contacts, ContactsContract.CommonDataKinds.Phone.NUMBER);
                        phoneNumberList.add(contactNumber);
                    }
                }
            }
        }
    }

    private String getColumnValueFromCursor(Cursor c, String columnName) {
        return c.getString(c.getColumnIndexOrThrow(columnName));
    }

    private Cursor getContacts(String contactId) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                             ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = '"
                + contactId + "'";

        return new ContextWrapper(this).getContentResolver().query(uri, projection, selection, null,
                null);
    }

    private void RefreshScheduledTaskStatusInView() {
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
        int scheduledPeriodInMilliseconds = getInMilliseconds(scheduledHour, scheduledMin);
        String phoneNumber = phoneNumberList.get(0);
        schedulePeriodicTask(phoneNumber, scheduledPeriodInMilliseconds);
        logAsServiceRunning(phoneNumber, scheduledHour, scheduledMin);
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
        int id = item.getItemId();
        if(id == R.id.clear_all_icon) {
            TextView timeTextBox = (TextView) findViewById(R.id.repeat_time_textbox);
            timeTextBox.setText(null);
            scheduledMin = 0;
            scheduledHour = 0;
            TextView contactTextBox = (TextView) findViewById(R.id.contact_text_box);
            contactTextBox.setText(null);
            phoneNumberList = new ArrayList<String>();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}