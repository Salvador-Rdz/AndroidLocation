package com.example.mapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StartupActivity extends AppCompatActivity {
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public boolean zonesFinish=false;
    public boolean interestFinish=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        enableMyLocation();

    }
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
        {
            new DownloadFileFromURL(
                    getApplicationContext(),"zones.json").execute("http://tampico.riverto.me/getZones.json");
            // TODO: add the other url downloads here, should be generic.
            new DownloadFileFromURL(
                    getApplicationContext(),"interest.json").execute("http://tampico.riverto.me/getLocations.json");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 1 :
                {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // TODO: add the other url downloads here, should be generic.
                    new DownloadFileFromURL(
                            getApplicationContext(),"zones.json").execute("http://tampico.riverto.me/getZones.json");
                    new DownloadFileFromURL(
                            getApplicationContext(),"interest.json").execute("http://tampico.riverto.me/getLocations.json");
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }
//
    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        ProgressDialog pd;
        String pathFolder = "";
        String pathFile = "";
        String fileName="";
        Context context;
        private DownloadFileFromURL(Context context, String fileName)
        {
            this.context=context;
            this.fileName=fileName;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(StartupActivity.this);
            pd.setTitle("Descargando información del mapa");
            pd.setMessage("Porfavor espere...");
            pd.setMax(100);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;

            try {
                pathFolder = getApplicationContext().getFilesDir().getPath();
                pathFile = pathFolder + File.separator +fileName;
                File futureStudioIconFile = new File(pathFolder);
                if(!futureStudioIconFile.exists()){
                    futureStudioIconFile.mkdirs();
                }

                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lengthOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                FileOutputStream output = new FileOutputStream(pathFile);

                byte data[] = new byte[1024]; //anybody know what 1024 means ?
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return pathFile;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pd.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (pd!=null) {
                pd.dismiss();
            }
            if(fileName=="interest.json")
            {
                interestFinish=true;
                if(zonesFinish)
                {
                    Intent intent2 = new Intent (context, MyLocationDemoActivity.class);
                    startActivity(intent2);
                }
            }
            else
            {
                zonesFinish=true;
                if(interestFinish)
                {
                    Intent intent2 = new Intent (context, MyLocationDemoActivity.class);
                    startActivity(intent2);
                }
            }

        }

    }
}
