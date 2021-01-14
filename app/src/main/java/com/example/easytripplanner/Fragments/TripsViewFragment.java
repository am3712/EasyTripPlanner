package com.example.easytripplanner.Fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.broadcastreceiver.AlarmReceiver;
import com.example.easytripplanner.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripsViewFragment extends Fragment {

    public static final String TRIP_NAME = "Name";
    public static final String TRIP_LOCATION_NAME = "LOCATION NAME";
    public static final String TRIP_LOC_LONGITUDE = "LOCATION LONGITUDE";
    public static final String TRIP_LOC_LATITUDE = "LOCATION LATITUDE";
    public static final String TRIP_ID = "ID";
    public static final String TRIP_HASH_CODE = "HASH CODE";
    private static final String TAG = "TripsViewFragment";
    public final static String LIST_STATE_KEY = "recycler_list_state";
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm aa");

    private TripRecyclerViewAdapter viewAdapter;
    private ArrayList<Trip> trips;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private ValueEventListener listener;
    private Query queryReference;

    Parcelable listState;

    public TripsViewFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trips = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        initQueryAndListener();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_trip, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            recyclerView = (RecyclerView) view;
            viewAdapter = new TripRecyclerViewAdapter(getContext(), trips, item -> {

            });
            recyclerView.setAdapter(viewAdapter);
            //recyclerView.setAdapter(new TripRecyclerViewAdapter(games, item -> ((Communicator) getActivity()).openGame(item)));
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = recyclerView.getLayoutManager();

    }

    private void initQueryAndListener() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;


        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference currentUserRef = null;
        if (userId != null) {
            currentUserRef = database.getReference("Users").child(userId);
            currentUserRef.keepSynced(true);
        }

        //get upcoming trips
        queryReference = currentUserRef
                .orderByChild("status")
                .startAt(TRIP_STATUS.FORGOTTEN.name())
                .endAt(TRIP_STATUS.UPCOMING.name());

        DatabaseReference finalCurrentUserRef = currentUserRef;

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calendar calendar = Calendar.getInstance();
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        calendar.setTimeInMillis(trip.timeInMilliSeconds);
                        trip.setDate(formatter.format(calendar.getTime()));

                        if (trip.timeInMilliSeconds < System.currentTimeMillis())
                            finalCurrentUserRef.child(trip.pushId).child("status").setValue("FORGOTTEN");

                        trips.add(trip);
                        viewAdapter.notifyDataSetChanged();
                        count++;
                        if (count >= snapshot.getChildrenCount()) {
                            Collections.sort(trips);
                            viewAdapter.notifyDataSetChanged();
                            checkAlarm();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void checkAlarm() {
        //save Shared Preferences
        SharedPreferences sharedPref = Objects.requireNonNull(getContext()).getSharedPreferences("Save", MODE_PRIVATE);

        //set Alarm
        AlarmManager alarmMgr = (AlarmManager) Objects.requireNonNull(getActivity()).getSystemService(ALARM_SERVICE);

        for (Trip t : trips) {

            //first scenario if trip is forgotten then do not fire and if found in sharedPreferences delete it
            if (t.status.equals("FORGOTTEN")) {
                if (sharedPref.contains(t.pushId)) {
                    //delete it from sharedPreference
                    sharedPref.edit().remove(t.pushId).apply();
                }
            } else if (!sharedPref.contains(t.pushId)) {

                //save trips id and trigger time in sharedPreference
                Log.i(TAG, "checkAlarm: trip name: " + t.name + ", fire alarm");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(t.pushId, t.timeInMilliSeconds);
                editor.apply();

                final Intent intent = new Intent(getContext(), AlarmReceiver.class);

                intent.putExtra(TRIP_NAME, t.name);
                intent.putExtra(TRIP_ID, t.pushId);
                intent.putExtra(TRIP_HASH_CODE, t.pushId.hashCode());
                Log.i(TAG, "checkAlarm: longitude: " + t.locationTo.longitude);
                Log.i(TAG, "checkAlarm: latitude: " + t.locationTo.latitude);
                intent.putExtra(TRIP_LOCATION_NAME, t.locationTo.Address);
                intent.putExtra(TRIP_LOC_LONGITUDE, t.locationTo.longitude);
                intent.putExtra(TRIP_LOC_LATITUDE, t.locationTo.latitude);


                PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(getContext(), t.pushId.hashCode(),
                        intent, PendingIntent.FLAG_NO_CREATE);
                if (notifyPendingIntent == null)
                    notifyPendingIntent = PendingIntent.getBroadcast
                            (getContext(), t.pushId.hashCode(), intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                if (t.repeating.equals("No Repeated")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.timeInMilliSeconds, notifyPendingIntent);
                    } else {
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, t.timeInMilliSeconds, notifyPendingIntent);
                    }
                } else {
                    long repeatInterval;
                    long ONE_DAY = 86400000;
                    switch (t.repeating) {
                        case "Repeated Daily":
                            repeatInterval = ONE_DAY;
                            break;
                        case "Repeated weekly":
                            repeatInterval = ONE_DAY * 7;
                            break;
                        case "Repeated Monthly":
                            repeatInterval = ONE_DAY * 7 * 4;
                            break;
                        default:
                            repeatInterval = 0;
                    }

                    alarmMgr.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            t.timeInMilliSeconds,
                            repeatInterval,
                            notifyPendingIntent);
                }
            }
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save list state
        listState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, listState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Retrieve list state and list/item positions
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        queryReference.addListenerForSingleValueEvent(listener);
        if (listState != null) {
            mLayoutManager.onRestoreInstanceState(listState);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        queryReference.removeEventListener(listener);
        trips.clear();
    }

    public enum TRIP_STATUS {
        DONE,
        FORGOTTEN,
        CANCELED,
        UPCOMING
    }
}