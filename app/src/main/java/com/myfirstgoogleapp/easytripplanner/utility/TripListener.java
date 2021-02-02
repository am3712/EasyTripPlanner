package com.myfirstgoogleapp.easytripplanner.utility;

import com.myfirstgoogleapp.easytripplanner.models.Trip;

public interface TripListener {
    void edit(String tripId);
    void delete(Trip trip);
    void startNav(Trip trip);
    void cancel(String id);
    void showNote(String id);

}
