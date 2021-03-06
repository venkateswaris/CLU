package com.example.venkateswaris.clu;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LocationUpdateService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;

    public LocationUpdateService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            setMobileDataEnabled(this, true);
            connectGoogleApiClient();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Bundle extras = intent.getExtras();
        Address address = startLocationUpdates();
        String addressString = getStringMessageFrom(address);
        String phoneNumber = extras.getString("phoneNumber");
        Log.i("Sent sms", phoneNumber);
        Log.i("address", addressString);
        sendSMS(phoneNumber, addressString);
        mGoogleApiClient.disconnect();
        setMobileDataEnabled(this, false);
    }

    private void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
            while(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() != enabled)
            {

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private String getStringMessageFrom(Address address) {
        return "subLocality " + address.getSubLocality()
                + ". subAdminArea " + address.getSubAdminArea() + ". City " + address.getLocality();
    }

    private void connectGoogleApiClient() throws InterruptedException {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        while (mGoogleApiClient.isConnected() != true) {
            mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, LocationUpdateService.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    protected Address startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Address currentAddress = getCurrentAddress();
        return currentAddress;
    }

    private Address getCurrentAddress() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Geocoder geocoder =
                new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses != null ? addresses.get(0) : null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("Message", "GoogleAPIClient Connection Successful");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("Message", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("Message", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
