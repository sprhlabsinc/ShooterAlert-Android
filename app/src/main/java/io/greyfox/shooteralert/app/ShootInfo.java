package io.greyfox.shooteralert.app;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by b on 1/13/2017.
 */

public class ShootInfo implements Serializable{
    public int id;
    public String incident_date;
    public String state;
    public String city;
    public String address;
    public int killed;
    public int injured;
    public String url1;
    public String url2;
    public double latitude;
    public double longitude;
    public double distance;
    public boolean inside;

    public ShootInfo() {

    }
}
