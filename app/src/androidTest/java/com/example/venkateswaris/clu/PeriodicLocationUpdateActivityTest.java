package com.example.venkateswaris.clu;

import android.content.Intent;
import android.provider.ContactsContract;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.Button;

public class PeriodicLocationUpdateActivityTest extends ActivityInstrumentationTestCase2<PeriodicLocationUpdateActivity> {


    PeriodicLocationUpdateActivity activity;

    public PeriodicLocationUpdateActivityTest() {
        super(PeriodicLocationUpdateActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    public void testShouldSchedulePeriodicActivity() throws Exception{
        activity = getActivity();
                final Button trackButton = (Button) activity.findViewById(R.id.track_button);
                TouchUtils.clickView(this, trackButton);
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

//        mActivityMonitor = getInstrumentation();
//        final Intent launchIntent = activity.wait;
//        PeriodicLocationUpdateActivity activity1 = getActivity();
//        assertNotNull("Intent was null", launchIntent);
//        assertTrue(isFinishCalled());
////                activity.findViewById(R.id.contact_image_button).performClick();
//        activity.runOnUiThread(runnable);
        assertNotNull("Intent was null", activity);
//        final Intent launchIntent =  getStartedActivityIntent();
//        assertNotNull("Intent was null", launchIntent);
//        assertTrue(isFinishCalled());
//
        assertEquals(1, 2);
    }
}
