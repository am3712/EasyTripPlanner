package com.example.easytripplanner.models;

import java.util.List;

public class Trip {
    public String name;
    public TripLocation locationFrom;
    public TripLocation locationTo;
    public String date;
    public String status;
    public String type;
    public String repeating;
    public List<Note> notes;
    public String pushId;
    public String time;


    public Trip() {
    }

    public Trip(String name, TripLocation locationFrom, TripLocation locationTo, String date, String status, String type, String repeating, List<Note> notes, String pushId, String time) {
        this.name = name;
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
        this.date = date;
        this.status = status;
        this.type = type;
        this.repeating = repeating;
        this.notes = notes;
        this.pushId = pushId;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "name='" + name + '\'' + "\n" +
                ", locationFrom=" + locationFrom + "\n" +
                ", locationTo=" + locationTo + "\n" +
                ", date='" + date + '\'' + "\n" +
                ", status='" + status + '\'' + "\n" +
                ", type='" + type + '\'' + "\n" +
                ", repeating='" + repeating + '\'' + "\n" +
                ", notes=" + notes + "\n" +
                ", pushId='" + pushId + '\'' + "\n" +
                ", time='" + time + '\'' +
                '}';
    }
}
