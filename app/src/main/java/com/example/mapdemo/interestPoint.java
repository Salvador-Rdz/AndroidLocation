package com.example.mapdemo;

import com.google.android.gms.maps.model.LatLng;

public class interestPoint {
    LatLng position;
    String title;
    String description;
    String imageRoute;

    interestPoint(LatLng position, String title, String description, String imageRoute)
    {
        this.position=position;
        this.title=title;
        this.description=description;
        this.imageRoute=imageRoute;
    }
}
