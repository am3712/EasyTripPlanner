package com.example.easytripplanner.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class Trip implements Serializable, Comparable<Trip> {
    public String name;
    public TripLocation locationFrom;
    public TripLocation locationTo;
    public String status;
    public String type;
    public String repeating;
    public String pushId;
    public Long timeInMilliSeconds;
    private String date;


    public Trip() {
    }

    public Trip(String name, TripLocation locationFrom, TripLocation locationTo, String status, String type, String repeating, String pushId, Long timeInMilliSeconds, String date) {
        this.name = name;
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
        this.status = status;
        this.type = type;
        this.repeating = repeating;
        this.pushId = pushId;
        this.timeInMilliSeconds = timeInMilliSeconds;
        this.date = date;
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
}
