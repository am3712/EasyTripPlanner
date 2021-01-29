package com.myfirstgoogleapp.easytripplanner.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class TripLocation implements Parcelable {
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


    protected TripLocation(Parcel in) {
        Address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TripLocation> CREATOR = new Parcelable.Creator<TripLocation>() {
        @Override
        public TripLocation createFromParcel(Parcel in) {
            return new TripLocation(in);
        }

        @Override
        public TripLocation[] newArray(int size) {
            return new TripLocation[size];
        }
    };
}