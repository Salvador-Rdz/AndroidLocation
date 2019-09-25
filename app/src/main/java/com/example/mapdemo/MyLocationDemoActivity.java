/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.PolyUtil;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.*;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MyLocationDemoActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap; //map to draw on
    private FusedLocationProviderClient fusedLocationClient; //client that manages the location updater
    // The id of the group.

    private static final String ANDROID_CHANNEL_ID = "com.chikeandroid.tutsplustalerts.ANDROID";
    private static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    private LatLng currentLocation; //vector that holds the current location of the users
    //50 by 50 seems to be usual size for markers, 70 looks better
    private int markerHeight = 70;
    private int markerWidth = 70;
    private ArrayList<interestPoint> interestPoints = new ArrayList<>();

    private NotificationManager nm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Mos of this is initialization stuff, don't fiddle with this.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //initializes the location services to get the last known location of device.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //Needed to get the object to manage notifications
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Setup for notification channels.
        createNotificationChannel();
        loadInterestPoints();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //gets the map
        mMap = map;
        //disables the "mylocation" button.
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle_retro));

        } catch (Resources.NotFoundException e) {

        }

         //gets permission for location usage
        enableMyLocation();

        //starts listener for location changes and gets the last know location of the device (while also requesting it)
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null)
                {
                    //gets the latitude and longitude of the location object to translate it to google maps terms
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    //moves the camera to the location with a zoom of 18 (1 to 29)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,18.0f));

                }
                //TODO: Check if you can remove these .add's to somewhere else, the context of the inner event causes problems

                //adds the custom marker, has title, description and the icon we just resized.
                for(int i=0; i<interestPoints.size();i++) {
                    //Resizing the image and getting the route.
                    BitmapDescriptor markerIcon= bitmapResize(getResources().getIdentifier(interestPoints.get(i).imageRoute,
                            "drawable", getPackageName()),markerWidth,markerHeight);
                    mMap.addMarker(new MarkerOptions().
                                    position(interestPoints.get(i).position).
                                    title(interestPoints.get(i).title).
                                    snippet(interestPoints.get(i).description).
                                    icon(markerIcon));
                }
                //Adds a circle, will be used for the "danger zones". self explanatory.
                //color.parsecolor and then a hex code allows for transparency, first 2 values dictate how transparent
                //FF=Solid 00=transparent
                //22.381490, -97.901634 top bit
                //22.382355, -97.902083 middle bit
                //22.381296, -97.901670 bottom bit
                LatLng pol1 = new LatLng(22.382467, -97.902399);
                LatLng pol2 = new LatLng(22.382382, -97.901946);
                LatLng pol3 = new LatLng(22.382278, -97.901950);
                LatLng pol4 = new LatLng(22.382345, -97.902433);
                ArrayList<LatLng> polygon = new ArrayList<>();
                polygon.add(pol1); polygon.add(pol2);polygon.add(pol3);polygon.add(pol4);
                mMap.addPolygon(new PolygonOptions()
                                .add(pol1,pol2,pol3,pol4)
                                .strokeWidth(2.0f)
                                .strokeColor(Color.RED)
                                .fillColor(Color.parseColor("#50ff0000")));

                if(PolyUtil.containsLocation(currentLocation,polygon,false))
                {
                    sendNotification();
                }

            }
        });

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ANDROID_CHANNEL_NAME;
            String description = ANDROID_CHANNEL_NAME;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ANDROID_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void sendNotification (){
        //Creating the event that happens when the user clicks on the notification
        Intent intent = new Intent(this, MyLocationDemoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //Builder that manages the looks and functionality of the notification, there's way more options.
        //Later on, fiddle with this.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                .setSmallIcon(R.drawable.custom_info_bubble)
                .setContentTitle("You're entering a danger zone")
                .setContentText("It is recommended to take alternate routes or take precaution if crossing this area is needed")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);
        //The actual line that sends the notification
        nm.notify(1234,builder.build());
    }

    public void loadInterestPoints(){
        try
        {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray userArray = obj.getJSONArray("points");
            for (int i = 0; i < userArray.length(); i++) {
                // create a JSONObject for fetching single user data
                JSONObject pointsDetail = userArray.getJSONObject(i);
                interestPoints.add(new interestPoint(
                        new LatLng(pointsDetail.getDouble("lat"),pointsDetail.getDouble("long")),
                        pointsDetail.getString("title"),
                        pointsDetail.getString("description"),
                        pointsDetail.getString("imageroute")
                ));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("test_interest_point.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    //receives an Image held in the res/drawable/ folder then resizes and returns as a bitmap.
    public BitmapDescriptor bitmapResize(int id, int width, int height )
    {
        Bitmap b = BitmapFactory.decodeResource(getResources(),id);
        Bitmap marker = Bitmap.createScaledBitmap(b,width,height,false);
        BitmapDescriptor markerIcon=  BitmapDescriptorFactory.fromBitmap(marker);
        return markerIcon;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}
