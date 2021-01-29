package com.example.easytripplanner.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class Trip implements Comparable<Trip>, Parcelable {
    public String name;
    public TripLocation locationFrom;
    public TripLocation locationTo;
    public String status;
    public String type;
    public String repeating;
    public String pushId;
    public Long timeInMilliSeconds;
    public Long dateInMilliSeconds;
    public boolean isUpdated;
    private String date;


    public Trip() {
    }

    public Trip(String name, TripLocation locationFrom, TripLocation locationTo, String status, String type,
                String repeating, String pushId, Long timeInMilliSeconds, Long dateInMilliSeconds, String date, boolean isUpdated) {
        this.name = name;
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
        this.status = status;
        this.type = type;
        this.repeating = repeating;
        this.pushId = pushId;
        this.timeInMilliSeconds = timeInMilliSeconds;
        this.dateInMilliSeconds = dateInMilliSeconds;
        this.date = date;
        this.isUpdated = isUpdated;
    }

    @Override
    public @NotNull String toString() {
        return "Trip{" +
                "name='" + name + '\'' + "\n" +
                ", locationFrom=" + locationFrom + "\n" +
                ", locationTo=" + locationTo + "\n" +
                ", status='" + status + '\'' + "\n" +
                ", type='" + type + '\'' + "\n" +
                ", repeating='" + repeating + '\'' + "\n" +
                ", pushId='" + pushId + '\'' + "\n" +
                ", timeInMilliSeconds=" + timeInMilliSeconds +
                ", dateInMilliSeconds=" + dateInMilliSeconds +
                '}';
    }

    @Override
    public int compareTo(Trip o) {
        return this.timeInMilliSeconds.compareTo(o.timeInMilliSeconds);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    protected Trip(Parcel in) {
        name = in.readString();
        locationFrom = (TripLocation) in.readValue(TripLocation.class.getClassLoader());
        locationTo = (TripLocation) in.readValue(TripLocation.class.getClassLoader());
        status = in.readString();
        type = in.readString();
        repeating = in.readString();
        pushId = in.readString();
        timeInMilliSeconds = in.readByte() == 0x00 ? null : in.readLong();
        dateInMilliSeconds = in.readByte() == 0x00 ? null : in.readLong();
        date = in.readString();
        isUpdated = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeValue(locationFrom);
        dest.writeValue(locationTo);
        dest.writeString(status);
        dest.writeString(type);
        dest.writeString(repeating);
        dest.writeString(pushId);
        if (timeInMilliSeconds == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(timeInMilliSeconds);
        }
        if (dateInMilliSeconds == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(dateInMilliSeconds);
        }
        dest.writeString(date);
        dest.writeByte((byte) (isUpdated ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}