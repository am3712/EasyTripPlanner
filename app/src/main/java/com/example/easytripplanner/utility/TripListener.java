package com.example.easytripplanner.utility;

import com.example.easytripplanner.models.Trip;

public interface TripListener {
    void editItem(String tripId);
    void deleteItem(Trip trip);
    void startNav(Trip trip);
}
