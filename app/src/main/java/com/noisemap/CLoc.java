package com.noisemap;

import com.baidu.location.BDLocation;

import java.io.Serializable;

/**
 * Created by yp on 2017/6/8.
 */

public class CLoc implements Serializable{
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int coord_type;

    private int db;

    private static final long serialVersionUID=1L;

    CLoc(){

    }

    public void setCLoc(BDLocation location){
        name=location.getLocationDescribe();
        address=location.getAddrStr();
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        switch (location.getCoorType()){
            case "wgs84":
                coord_type=1;
                break;
            case "gcj02":
                coord_type=2;
                break;
            case "bd09ll":
                coord_type=3;
                break;
            case"db09mc":
                coord_type=4;
                break;
            default:
                coord_type=0;
                break;
        }


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

    public int getCoord_type() {
        return coord_type;
    }

    public void setCoord_type(int coord_type) {
        this.coord_type = coord_type;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }


}
