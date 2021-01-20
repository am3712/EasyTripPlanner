package com.example.easytripplanner.Fragments;

import com.mapbox.geojson.Point;

public class TripRoute {
    private final Point origin;
    private final Point destination;

    public TripRoute(Point origin, Point destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public Point getOrigin() {
        return origin;
    }


    public Point getDestination() {
        return destination;
    }

}