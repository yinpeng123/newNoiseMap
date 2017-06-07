package com.noisemap;

import java.io.Serializable;

/**
 * Created by yp on 2017/6/7.
 */

public class CPoi implements Serializable {
    public double latitude;
    public double longitude;
    public String name;
    public String address;
    public int db;
    public int coord_type;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public void setCoord_type(int coord_type) {
        this.coord_type = coord_type;
    }

    public int getCoord_type() {
        return coord_type;
    }
}
