package com.example.easytripplanner.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.easytripplanner.utility.Parcelables;
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

    public static final String TRIP = "TRIP";
    public static final String TRIP_ID = "Trip Id";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm aa");

    private Intent mapIntent;


    private final ActivityResultLauncher<Intent> overlayActivityResultLauncher;


    private TripRecyclerViewAdapter mAdapter;
    private ArrayList<Trip> trips;
    private ChildEventListener mRetrieveTripsListener;
    private Query queryReference;
    DatabaseReference currentUserRef;
    private TripListener menuItemListener;
    private FragmentUpcomingBinding binding;
    private String navigationTripId;


    public UpcomingFragment() {
        overlayActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        startFloatingService();
                        //start Trip in maps
                        this.startActivity(mapIntent);
                        //finish activity (App)
                        requireActivity().finishAndRemoveTask();
                    } else
                        Toast.makeText(requireContext(), "Floating service permissions denied", Toast.LENGTH_SHORT).show();
                });
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
                .equalTo(TRIP_STATUS.UPCOMING.name());

        Calendar calendar = Calendar.getInstance();
        mRetrieveTripsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Trip trip = snapshot.getValue(Trip.class);
                if (trip != null && trip.timeInMilliSeconds != null && trip.dateInMilliSeconds != null) {
                    binding.noData.setVisibility(View.GONE);
                    calendar.setTimeInMillis(trip.timeInMilliSeconds + trip.dateInMilliSeconds);
                    trip.setDate(formatter.format(calendar.getTime()));
                    trips.add(trip);
                    Collections.sort(trips);
                    mAdapter.notifyDataSetChanged();
                    checkAlarm(trip);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Timber.i("onChildChanged called");
                clear();
                binding.noData.setVisibility(View.VISIBLE);
                queryReference.addChildEventListener(mRetrieveTripsListener);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Timber.i("onChildRemoved called");
                String id = snapshot.child("pushId").getValue(String.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    trips.removeIf(trip -> trip.pushId.equals(id));
                } else {
                    for (Iterator<Trip> iterator = trips.iterator(); iterator.hasNext(); ) {
                        if (iterator.next().pushId.equals(id)) {
                            iterator.remove();
                            if (trips.size() == 0)
                                binding.noData.setVisibility(View.VISIBLE);
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
                //cancel remainder
                cancelRemainder(trip.pushId);

                //remove trip
                currentUserRef.child(trip.pushId).removeValue();

                Toast.makeText(getContext(), "deleting success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void startNav(Trip trip) {
                startNavigation(trip);
            }

            @Override
            public void cancel(String id) {
                //cancel remainder
                cancelRemainder(id);

                //set new Status
                currentUserRef.child(id).child("status").setValue(TRIP_STATUS.CANCELED.name());

                Toast.makeText(requireContext(), "Trip Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void showNote(String id) {
                Navigation.findNavController(binding.getRoot())
                        .navigate(UpcomingFragmentDirections.actionUpcomingFragmentToAddNote(id));
            }
        };
    }


    private void checkAlarm(Trip t) {

        //set Alarm
        AlarmManager alarmMgr = (AlarmManager) requireContext().getSystemService(ALARM_SERVICE);

        final Intent intent = new Intent(requireContext(), AlarmReceiver.class);

        Timber.i("trip is updated: %s", t.isUpdated());

        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(requireContext(), t.pushId.hashCode(),
                intent, PendingIntent.FLAG_NO_CREATE);

        //check if alarm is exist or not
        if (notifyPendingIntent == null || t.isUpdated()) {

            //alarm is not exist add it
            if (t.isUpdated()) {
                currentUserRef.child(t.pushId).child("IsUpdated").setValue(false);
                Timber.i("checkAlarm: %s is exist in alarm manager and will updated..", t.name);
            } else
                Timber.i("checkAlarm: %s is not exist in alarm manager..", t.name);

            intent.putExtra(TRIP, Parcelables.toByteArray(t));

            notifyPendingIntent = PendingIntent.getBroadcast
                    (requireContext(), t.pushId.hashCode(), intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            if (t.repeating.equals("No Repeated")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmMgr.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            t.dateInMilliSeconds + t.timeInMilliSeconds,
                            notifyPendingIntent);
                } else {
                    alarmMgr.setExact(
                            AlarmManager.RTC_WAKEUP,
                            t.dateInMilliSeconds + t.timeInMilliSeconds,
                            notifyPendingIntent);
                }
            } else {

                alarmMgr.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        t.dateInMilliSeconds + t.timeInMilliSeconds,
                        getRepeatInterval(t.repeating),
                        notifyPendingIntent);
            }
        } else
            Timber.i("checkAlarm: %s is exist in alarm manager..", t.name);

    }

    public enum TRIP_STATUS {
        DONE,
        CANCELED,
        UPCOMING
    }

    @Override
    public void onStart() {
        super.onStart();
        queryReference.addChildEventListener(mRetrieveTripsListener);
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

        navigationTripId = trip.pushId;

        //open on google maps
        Uri uri = Uri.parse("google.navigation:q=" + trip.locationTo.latitude + "," + trip.locationTo.longitude);
        mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);


            //cancel trigger remainder
            cancelRemainder(trip.pushId);

            //set new Status
            changeTripStatus(trip);

            currentUserRef.child(trip.pushId).get().addOnCompleteListener(task -> {
                //start floating service
                if (Objects.requireNonNull(task.getResult()).hasChild("notes")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(requireContext())) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + requireContext().getPackageName()));
                            overlayActivityResultLauncher.launch(intent);
                            return;
                        }
                    }
                    startFloatingService();
                    //start Trip in maps
                    this.startActivity(mapIntent);
                    //finish activity (App)
                    requireActivity().finishAndRemoveTask();
                }
            });


        } else {
            Toast.makeText(getContext(), "please install google maps!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFloatingService() {
        Intent noteIntent = new Intent(requireContext(), FloatingViewService.class);
        noteIntent.putExtra(TRIP_ID, navigationTripId);
        requireContext().startService(noteIntent);
    }

    private void changeTripStatus(Trip trip) {
        if (trip != null) {
            if (!trip.repeating.equalsIgnoreCase("No Repeated"))
                currentUserRef.child(trip.pushId).child("dateInMilliSeconds").setValue(trip.dateInMilliSeconds + getRepeatInterval(trip.repeating));
            else
                currentUserRef.child(trip.pushId).child("status").setValue(TRIP_STATUS.DONE.name());
        }
    }

    private void cancelRemainder(String pushId) {

        //set Alarm
        AlarmManager alarmMgr = (AlarmManager) requireContext().getSystemService(ALARM_SERVICE);

        final Intent intent = new Intent(requireContext(), AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), pushId.hashCode(),
                intent, PendingIntent.FLAG_NO_CREATE);

        if (alarmMgr != null) {
            alarmMgr.cancel(pendingIntent);
            Timber.i("Alarm manger cancel trip");
        }
    }

    private void clear() {
        queryReference.removeEventListener(mRetrieveTripsListener);
        trips.clear();
        Objects.requireNonNull(binding.recyclerView.getAdapter()).notifyDataSetChanged();
    }

    public static long getRepeatInterval(String repeating) {
        long ONE_DAY = 86400000;
        switch (repeating) {
            case "Repeated Daily":
                return ONE_DAY;
            case "Repeated weekly":
                return ONE_DAY * 7;
            case "Repeated Monthly":
                return ONE_DAY * 7 * 4;
            default:
                throw new IllegalStateException("Unexpected value: " + repeating);
        }
    }
}