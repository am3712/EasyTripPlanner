package com.example.easytripplanner.utility;

import com.example.easytripplanner.models.Trip;

public interface TripListener {
    void edit(String tripId);
    void delete(Trip trip);
    void startNav(Trip trip);
    void cancel(String id);
    void showNote(String id);

}
