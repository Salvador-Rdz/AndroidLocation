package com.example.mapdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.Tec.Tec_Final_Ar.UnityPlayerActivity;
//Empty activity that functions only as the intermediary for the map, since unity closes the activity that called it.
public class unityLoader extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unity_loader);
        Intent intent2 = new Intent (getApplicationContext(), UnityPlayerActivity.class);
        startActivity(intent2);
    }
}
