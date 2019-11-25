package com.example.mapdemo;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class DangerZone {
    LatLng p1;
    LatLng p2;
    LatLng p3;
    LatLng p4;
    String title;
    String description;
    String type;

    DangerZone(String type,LatLng p1, LatLng p2, LatLng p3, LatLng p4, String title, String description)
    {
        this.type= type;
        this.p1=p1;
        this.p2=p2;
        this.p3=p3;
        this.p4=p4;
        this.title=title;
        this.description=description;
    }

    public ArrayList<LatLng> getPolygon()
    {
        ArrayList<LatLng> dangerZones = new ArrayList<>();
        dangerZones.add(p1);
        dangerZones.add(p2);
        dangerZones.add(p3);
        dangerZones.add(p4);
        return dangerZones;
    }
}
