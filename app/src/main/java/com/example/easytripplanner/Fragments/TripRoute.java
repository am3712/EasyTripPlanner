package com.example.easytripplanner.Fragments;

import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

public class TripRoute {
    private final Point origin;
    private final Point destination;
    private List<Point> points;

    public TripRoute(Point origin, Point destination) {
        points = new ArrayList<>();
        this.origin = origin;
        this.destination = destination;

        points.add(origin);
        points.add(destination);
    }

    public Point getOrigin() {
        return origin;
    }


    public Point getDestination() {
        return destination;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}