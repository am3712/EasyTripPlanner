package com.example.easytripplanner.Fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.easytripplanner.R;
import com.example.easytripplanner.adapters.TripRecyclerViewAdapter;
import com.example.easytripplanner.databinding.FragmentUpcomingBinding;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.services.AlarmReceiver;
import com.example.easytripplanner.services.FloatingViewService;
import com.example.easytripplanner.utility.TripListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingFragment extends Fragment {

    public static final String TRIP_NAME = "Name";
    public static final String TRIP_LOCATION_NAME = "LOCATION NAME";
    public static final String TRIP_LOC_LONGITUDE = "LOCATION LONGITUDE";
    public static final String TRIP_LOC_LATITUDE = "LOCATION LATITUDE";
    public static final String TRIP_ID = "ID";
    public static final String TRIP_HASH_CODE = "HASH CODE";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm aa");

    private TripRecyclerViewAdapter mAdapter;
    private ArrayList<Trip> trips;
    private ChildEventListener mRetrieveTripslistener;
    private Query queryReference;
    DatabaseReference currentUserRef;

    private TripListener menuItemListener;

    private FragmentUpcomingBinding binding;


    public UpcomingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trips = new ArrayList<>();
        initQueryAndListener();
        initPopUpMenuItemListener();
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUpcomingBinding.inflate(inflater, container, false);
        mAdapter = new TripRecyclerViewAdapter(getContext(), trips, true, menuItemListener);
        binding.recyclerView.setAdapter(mAdapter);

        binding.fbAddTrip.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot())
                        .navigate(UpcomingFragmentDirections
                                .actionUpcomingFragmentToAddTripFragment(getString(R.string.add_trip_title))));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.i("onViewCreated: ");
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Navigation.findNavController(view).popBackStack();
            Navigation.findNavController(view).navigate(R.id.loginFragment);
        }
    }

    public void initQueryAndListener() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;


        FirebaseDatabase database = FirebaseDatabase.getInstance();

        currentUserRef = null;
        currentUserRef = database.getReference("Users").child(userId);
        currentUserRef.keepSynced(true);

        //get upcoming trips
        queryReference = currentUserRef
                .orderByChild("status")
                .startAt(TRIP_STATUS.FORGOTTEN.name())
                .endAt(TRIP_STATUS.UPCOMING.name());

        DatabaseReference finalCurrentUserRef = currentUserRef;

        Calendar calendar = Calendar.getInstance();
        mRetrieveTripslistener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Trip trip = snapshot.getValue(Trip.class);
                if (trip != null && trip.timeInMilliSeconds != null) {
                    calendar.setTimeInMillis(trip.timeInMilliSeconds);
                    trip.setDate(formatter.format(calendar.getTime()));

                    if (trip.timeInMilliSeconds < System.currentTimeMillis()) {
                        finalCurrentUserRef.child(trip.pushId).child("status").setValue("FORGOTTEN");
                    }
                    trips.add(trip);
                    Collections.sort(trips);
                    mAdapter.notifyDataSetChanged();
                    checkAlarm(trip);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                clear();
                queryReference.addChildEventListener(mRetrieveTripslistener);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String id = snapshot.child("pushId").getValue(String.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    trips.removeIf(trip -> trip.pushId.equals(id));
                } else {
                    for (Iterator<Trip> iterator = trips.iterator(); iterator.hasNext(); ) {
                        if (iterator.next().pushId.equals(id)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                Objects.requireNonNull(binding.recyclerView.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private void initPopUpMenuItemListener() {
        menuItemListener = new TripListener() {
            @Override
            public void edit(String tripId) {
                Navigation.findNavController(binding.getRoot())
                        .navigate(UpcomingFragmentDirections
                                .actionUpcomingFragmentToAddTripFragment(getString(R.string.edit_trip_title))
                                .setID(tripId));
            }

            @Override
            public void delete(Trip trip) {
                currentUserRef.child(trip.pushId).removeValue((error, ref) ->
                        Toast.makeText(getContext(), "deleting success", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void startNav(Trip trip) {
                startNavigation(trip);
            }

            @Override
            public void cancel(String id) {
                //set new Status : Canceled
                currentUserRef.child(id).child("status").setValue(TRIP_STATUS.CANCELED.name());
                cancelRemainder(id);
                Toast.makeText(requireContext(), "Trip Canceled", Toast.LENGTH_SHORT).show();
            }
        };
    }


    private void checkAlarm(Trip t) {

        //set Alarm
        AlarmManager alarmMgr = (AlarmManager) requireActivity().getSystemService(ALARM_SERVICE);

        final Intent intent = new Intent(getContext(), AlarmReceiver.class);


        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(requireContext(), t.pushId.hashCode(),
                intent, PendingIntent.FLAG_NO_CREATE);
        //check if alarm is exist or not
        if (notifyPendingIntent == null) {

            //alarm is not exist add it

            Timber.i("checkAlarm: %s is not exist in alarm manager..", t.name);

            intent.putExtra(TRIP_NAME, t.name);
            intent.putExtra(TRIP_ID, t.pushId);
            intent.putExtra(TRIP_HASH_CODE, t.pushId.hashCode());
            intent.putExtra(TRIP_LOCATION_NAME, t.locationTo.Address);
            intent.putExtra(TRIP_LOC_LONGITUDE, t.locationTo.longitude);
            intent.putExtra(TRIP_LOC_LATITUDE, t.locationTo.latitude);


            notifyPendingIntent = PendingIntent.getBroadcast
                    (requireContext(), t.pushId.hashCode(), intent,
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
        } else
            Timber.i("checkAlarm: %s is exist in alarm manager..", t.name);


    }

    public enum TRIP_STATUS {
        DONE,
        FORGOTTEN,
        CANCELED,
        UPCOMING
    }

    @Override
    public void onStart() {
        super.onStart();
        queryReference.addChildEventListener(mRetrieveTripslistener);
    }

    @Override
    public void onStop() {
        super.onStop();
        clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @SuppressLint("QueryPermissionsNeeded")
    public void startNavigation(Trip trip) {

        //open on google maps
        Uri uri = Uri.parse("google.navigation:q=" + trip.locationTo.latitude + "," + trip.locationTo.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            //set new Status : DONE
            currentUserRef.child(trip.pushId).child("status").setValue(TRIP_STATUS.DONE.name());

            //cancel trigger remainder
            cancelRemainder(trip.pushId);

            //start Trip in maps
            this.startActivity(mapIntent);

            //start floating service
            requireActivity().startService(new Intent(getContext(), FloatingViewService.class));

            //finish activity (App)
            requireActivity().finish();

        } else {
            Toast.makeText(getContext(), "please install google maps!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRemainder(String pushId) {

        Intent intent = new Intent();

        AlarmManager alarmManager =
                (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(requireContext(), pushId.hashCode(), intent,
                        PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void clear() {
        queryReference.removeEventListener(mRetrieveTripslistener);
        trips.clear();
        Objects.requireNonNull(binding.recyclerView.getAdapter()).notifyDataSetChanged();
    }
}