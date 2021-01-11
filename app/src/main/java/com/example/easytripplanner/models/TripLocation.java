package com.example.easytripplanner.models;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
    public @NotNull String toString() {
        return "TripLocation{" +
                "Address='" + Address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripLocation location = (TripLocation) o;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0 &&
                Address.equals(location.Address);
    }

}
