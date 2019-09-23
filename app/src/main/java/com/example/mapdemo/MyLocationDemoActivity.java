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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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

    private LatLng currentLocation; //vector that holds the current location of the users
    private LatLng circleLocation;
    //50 by 50 seems to be usual size for markers
    private int markerHeight = 50;
    private int markerWidth = 50;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //initialization stuff, don't fiddle with this.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //initializes the location services to get the last known location of device.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        //gets the map
        mMap = map;
        //disables the "mylocation" button.
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
                    circleLocation = currentLocation;
                }
                //TODO: Check if you can remove these .add's to somewhere else, the context of the inner event causes problems
                BitmapDescriptor markerIcon= bitmapResize(R.drawable.concern,markerWidth,markerHeight);
                //adds the custom marker, has title, description and the icon we just resized.
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Title").snippet("Description").icon(markerIcon));
                //Adds a circle, will be used for the "danger zones". self explanatory.
                //color.parsecolor and then a hex code allows for transparency, first 2 values dictate how transparent
                //FF=Solid 00=transparent
                mMap.addCircle(new CircleOptions()
                        .center(circleLocation) //pivot where the center of the circle will be
                        .radius(20) //size of the circle
                        .strokeWidth(1.0f) //width of the outline
                        .strokeColor(Color.RED) //color of the outline
                        .fillColor(Color.parseColor("#50ff0000"))); //fill of the circle
            }
        });

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
