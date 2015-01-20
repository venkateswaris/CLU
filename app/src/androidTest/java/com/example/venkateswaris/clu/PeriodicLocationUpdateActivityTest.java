package com.example.venkateswaris.clu;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

public class PeriodicLocationUpdateActivityTest extends ActivityUnitTestCase<PeriodicLocationUpdateActivity> {


    PeriodicLocationUpdateActivity activity;
    public PeriodicLocationUpdateActivityTest() {
        super(PeriodicLocationUpdateActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @MediumTest
    public void ShouldCallCLUServiceOnTrackButtonClick(){
        final Button trackButton = (Button) activity.findViewById(R.id.track_button);
        trackButton.performClick();
        assertNotNull("Intent was null", activity);

        final Intent launchIntent =  getStartedActivityIntent();
        assertNotNull("Intent was null", launchIntent);
        assertTrue(isFinishCalled());
    }
}