package com.example.mseifriedsberger16.shoppinglist;

import java.io.Serializable;

/**
 * Created by mseifriedsberger16 on 26.03.2019.
 */

public class Shop implements Serializable{
    String name;
    Double lat, longi;

    public Shop(String name, Double lat, Double longi) {
        this.name = name;
        this.lat = lat;
        this.longi = longi;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLongi() {
        return longi;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", longi=" + longi +
                '}';
    }
}
