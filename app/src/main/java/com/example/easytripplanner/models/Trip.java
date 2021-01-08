package com.example.easytripplanner.models;

import java.util.List;

public class Trip {
    public String name;
    public String locationFrom;
    public String locationTo;
    public String date;
    public String status;
    public String type;
    public List<Note> notes;


    public Trip() {
    }

    public Trip(String name, String locationFrom, String locationTo) {
        this.name = name;
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
    }

    public Trip(String name, String locationFrom, String locationTo, String date, String status, String type, List<Note> notes) {
        this.name = name;
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
        this.date = date;
        this.status = status;
        this.type = type;
        this.notes = notes;
    }
}
