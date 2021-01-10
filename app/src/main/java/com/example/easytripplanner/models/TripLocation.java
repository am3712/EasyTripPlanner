package com.example.easytripplanner.models;

public class TripLocation {
    public String Address;
    public double latitude;
    public double longitude;

    public TripLocation() {
    }

    public TripLocation(String address, double latitude, double longitude) {
        Address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "TripLocation{" +
                "Address='" + Address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
