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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;
import com.Tec.Tec_Final_Ar.UnityPlayerActivity;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.UniversalTimeScale;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.json.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


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
    private boolean requestingLocationUpdates = true;
    private GoogleMap mMap; //map to draw on
    private FusedLocationProviderClient fusedLocationClient; //client that manages the location updater
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private static final String ANDROID_CHANNEL_ID = "com.chikeandroid.tutsplustalerts.ANDROID";
    private static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    private LatLng currentLocation; //vector that holds the current location of the user
    //50 by 50 seems to be usual size for markers, 70 looks better
    private int markerHeight = 70;
    private int markerWidth = 70;
    private ArrayList<DangerZone> dangerZones = new ArrayList<>();
    private ArrayList<interestPoint> interestPoints = new ArrayList<>();
    private ArrayList<String> cupons = new ArrayList<>();
    private NotificationManager nm;
    private ProgressDialog pd;
    private boolean notification = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        copyAssets(); //this copies interest_points.json as a backup
        loadInterestPoints(); //this loads INTEREST.json, not interest_points.json. Check logcat errors if it it's crashing
        loadDangerZones();
        //Mos of this is initialization stuff, don't fiddle with this.


        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        RadioGroup reminder = (RadioGroup) findViewById(R.id.reminder);
        reminder.setVisibility(View.GONE);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //initializes the location services to get the last known location of device.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //Needed to get the object to manage notifications
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Setup for notification channels.
        createNotificationChannel();
        createLocationRequest();
        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent unity = new Intent(getApplicationContext(), UnityPlayerActivity.class);
                startActivity(unity);
                //debugging();
                //TODO: Change this at school
            }
        });
        Button btn5 = (Button) findViewById(R.id.button5);
        btn5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent cuenta = new Intent(getApplicationContext(),userInfo.class);
                startActivity(cuenta);
            }
        });
        Button btn4 = (Button) findViewById(R.id.button4);
        btn4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                finishAffinity();
            }
        });
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation= new LatLng(location.getLatitude(),location.getLongitude());
                }
            };
        };

    }

    public void debugging()
    {
        /*
        File[] files = getFilesDir().listFiles();
        String text="";
        for(File f :files)
        {
            text+=f.getName();
        }
        Toast.makeText(this,"files "+text,Toast.LENGTH_LONG).show();
        */
        /*
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String cupon = "";
        try {
            URL url = new URL("http://tampico.riverto.me/getCoupon");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Scanner scan = new Scanner(in);

            while (scan.hasNextLine())
            {
                cupon += scan.nextLine();
            }
        }
        catch (MalformedURLException e)
        {

        }
        catch (IOException e)
        {

        }
        RadioGroup reminder = (RadioGroup) findViewById(R.id.reminder);
        reminder.setVisibility(View.VISIBLE);
        TextView reminderText = (TextView) findViewById(R.id.textView3);
        reminderText.setText(cupon);
        Intent pop = new Intent(getApplicationContext(),CouponPopActivity.class);
        pop.putExtra("asdsa",cupon);
        startActivity(pop);
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }


    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
    }
    //TODO:
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
                            this, R.raw.mapa));

        } catch (Resources.NotFoundException e) {

        }

         //gets permission for location usage
        enableMyLocation();
        // ;
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

                //adds the custom marker, has title , description and the icon we just resized.
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
                for(int i = 0; i< dangerZones.size(); i++)
                {
                    if(dangerZones.get(i).type.equals("danger"))
                    {
                        mMap.addPolygon(new PolygonOptions()
                                .add(dangerZones.get(i).p1, dangerZones.get(i).p2,
                                        dangerZones.get(i).p3, dangerZones.get(i).p4)
                                .strokeWidth(2.0f)
                                .strokeColor(Color.RED)
                                .fillColor(Color.parseColor("#55eb4034")));
                    }
                    else if(dangerZones.get(i).type.equals("business"))
                    {
                        mMap.addPolygon(new PolygonOptions()
                                .add(dangerZones.get(i).p1, dangerZones.get(i).p2,
                                        dangerZones.get(i).p3, dangerZones.get(i).p4)
                                .strokeWidth(2.0f)
                                .strokeColor(Color.YELLOW)
                                .fillColor(Color.parseColor("#55ffef0f")));
                    }
                }
                //Declare the timer
                Timer t = new Timer();
                //Set the schedule function and rate
                t.scheduleAtFixedRate(new TimerTask()
                                      {
                                          @Override
                                          public void run() {
                                              locationCallback = new LocationCallback() {
                                                  @Override
                                                  public void onLocationResult(LocationResult locationResult) {
                                                      if (locationResult == null) {
                                                          return;
                                                      }
                                                      for (Location location : locationResult.getLocations()) {
                                                          currentLocation= new LatLng(location.getLatitude(),location.getLongitude());
                                                      }
                                                  };
                                              };
                                              //Checks the current danger zones lists, comparing their area with our current position
                                              for(int i = 0; i< dangerZones.size(); i++)
                                              {
                                                  if(PolyUtil.containsLocation(currentLocation, dangerZones.get(i).getPolygon(),false))
                                                  {
                                                      if(dangerZones.get(i).type.equals("danger"))
                                                      {
                                                          sendNotification(dangerZones.get(i).title,dangerZones.get(i).description);
                                                      }
                                                      else if(dangerZones.get(i).type.equals("business"))
                                                      {
                                                          if(notification)
                                                          {
                                                              //send popup
                                                              StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                                              StrictMode.setThreadPolicy(policy);
                                                              String cupon = "";
                                                              try {
                                                                  URL url = new URL("http://tampico.riverto.me/getCoupon");
                                                                  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                                                  InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                                                                  Scanner scan = new Scanner(in);

                                                                  while (scan.hasNextLine())
                                                                  {
                                                                      cupon += scan.nextLine();
                                                                  }
                                                              }
                                                              catch (MalformedURLException e)
                                                              {

                                                              }
                                                              catch (IOException e)
                                                              {

                                                              }
                                                              Intent pop = new Intent(getApplicationContext(),CouponPopActivity.class);
                                                              pop.putExtra("Texto",cupon);
                                                              startActivity(pop);
                                                              notification=false;
                                                          }
                                                      }
                                                  }
                                              }
                                          }

                                      },
                        //Set how long before to start calling the TimerTask (in milliseconds)
                        0,
                        //Set the amount of time between each execution (in milliseconds)
                        2000);
            }
        }
        );

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


    private void sendNotification (String title, String description){
        //Creating the event that happens when the user clicks on the notification
        Intent intent = new Intent(this, MyLocationDemoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //Builder that manages the looks and functionality of the notification, there's way more options.
        //Later on, fiddle with this.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                .setSmallIcon(R.drawable.custom_info_bubble)
                .setContentTitle(title)
                .setContentText(description+"It is recommended to take alternate routes or take precaution if crossing this area is needed")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);
        //The actual line that sends the notification
        nm.notify(1234,builder.build());
    }


    private void copyAssets() {
    Context Context = getApplicationContext();
    String DestinationFile = Context.getFilesDir().getPath() + File.separator + "interest_points.json";
    if (!new File(DestinationFile).exists()) {
        try {
            CopyFromAssetsToStorage(Context, "interest_points.json", DestinationFile);
            //Toast.makeText(this,"Worked",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(this,"Didn't work",Toast.LENGTH_LONG).show();
        }
    }
    else
    {
        //Toast.makeText(this,"File exists at: "+DestinationFile,Toast.LENGTH_LONG).show();
    }
    DestinationFile = Context.getFilesDir().getPath() + File.separator + "danger_zones.json";
    if (!new File(DestinationFile).exists()) {
        try {
            CopyFromAssetsToStorage(Context, "danger_zones.json", DestinationFile);
            //Toast.makeText(this,"Worked",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(this,"Didn't work",Toast.LENGTH_LONG).show();
        }
    }
    else
    {
        //Toast.makeText(this,"File exists at: "+DestinationFile,Toast.LENGTH_LONG).show();
    }
}
    private void CopyFromAssetsToStorage(Context Context, String SourceFile, String DestinationFile) throws IOException {
        InputStream IS = Context.getAssets().open(SourceFile);
        OutputStream OS = new FileOutputStream(DestinationFile);
        CopyStream(IS, OS);
        OS.flush();
        OS.close();
        IS.close();
    }
    private void CopyStream(InputStream Input, OutputStream Output) throws IOException {
        byte[] buffer = new byte[5120];
        int length = Input.read(buffer);
        while (length > 0) {
            Output.write(buffer, 0, length);
            length = Input.read(buffer);
        }
    }


    public void loadDangerZones(){
        try
        {
            JSONObject obj = new JSONObject(loadJSONFromAsset("zones.json"));
            //Toast.makeText(this,"json object contains:"+obj.toString(),Toast.LENGTH_LONG).show();
            JSONArray userArray = obj.getJSONArray("zones");
            for (int i = 0; i < userArray.length(); i++) {
                // create a JSONObject for fetching single user data
                JSONObject pointsDetail = userArray.getJSONObject(i);
                dangerZones.add(new DangerZone(pointsDetail.getString("type"),
                        new LatLng(pointsDetail.getJSONArray("p1").getDouble(0),
                                pointsDetail.getJSONArray("p1").getDouble(1)),
                        new LatLng(pointsDetail.getJSONArray("p2").getDouble(0),
                                pointsDetail.getJSONArray("p2").getDouble(1)),
                        new LatLng(pointsDetail.getJSONArray("p3").getDouble(0),
                                pointsDetail.getJSONArray("p3").getDouble(1)),
                        new LatLng(pointsDetail.getJSONArray("p4").getDouble(0),
                                pointsDetail.getJSONArray("p4").getDouble(1)),
                        pointsDetail.getString("title"),
                        pointsDetail.getString("description")));
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    public void loadInterestPoints(){
        try
        {
            String json =loadJSONFromAsset("interest.json");
            if(json.isEmpty()) json = loadJSONFromAsset("interest_points.json") ;
            JSONObject obj = new JSONObject(json);
            //Toast.makeText(this,"json object contains:"+obj.toString(),Toast.LENGTH_LONG).show();
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

    public String loadJSONFromAsset(String assetFilename) {
        String json = null;
        Context Context = getApplicationContext();
        String DestinationFile = Context.getFilesDir().getPath() + File.separator + assetFilename;
        try{

            FileInputStream is = new FileInputStream(new File(DestinationFile));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        //Toast.makeText(this, "content in: "+DestinationFile+" :"+json,Toast.LENGTH_LONG).show();
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
