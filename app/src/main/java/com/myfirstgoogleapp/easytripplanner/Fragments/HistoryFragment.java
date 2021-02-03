package com.myfirstgoogleapp.easytripplanner.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.myfirstgoogleapp.easytripplanner.R;
import com.myfirstgoogleapp.easytripplanner.adapters.TripRecyclerViewAdapter;
import com.myfirstgoogleapp.easytripplanner.databinding.FragmentHistoryBinding;
import com.myfirstgoogleapp.easytripplanner.models.Trip;
import com.myfirstgoogleapp.easytripplanner.utility.TripListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Objects;

import static com.myfirstgoogleapp.easytripplanner.Fragments.UpcomingFragment.LIST_STATE_KEY;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private DatabaseReference currentUserRef;

    private ArrayList<Trip> trips;
    private ChildEventListener listener;
    private Query queryReference;
    private TripListener mTripListener;

    private String userId;

    private Parcelable listState;
    private RecyclerView.LayoutManager mLayoutManager;

    public HistoryFragment() {

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

        // Retrieve list state and list/item positions
        if (savedInstanceState != null)
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        // Set the adapter
        TripRecyclerViewAdapter viewAdapter = new TripRecyclerViewAdapter(getContext(), trips, false, mTripListener);
        binding.recyclerView.setAdapter(viewAdapter);
        mLayoutManager = binding.recyclerView.getLayoutManager();
        return binding.getRoot();
    }

    private void initQueryAndListener() {
        userId = FirebaseAuth.getInstance().getUid();

        if (userId == null)
            return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        currentUserRef = null;
        currentUserRef = database.getReference("Users").child(userId);
        currentUserRef.keepSynced(true);


        queryReference = currentUserRef
                .orderByChild("status")
                .startAt(UpcomingFragment.TRIP_STATUS.CANCELED.name())
                .endAt(UpcomingFragment.TRIP_STATUS.DONE.name());


        Calendar calendar = Calendar.getInstance();
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Trip trip = snapshot.getValue(Trip.class);
                if (trip != null && trip.timeInMilliSeconds != null && trip.dateInMilliSeconds != null) {
                    binding.noData.setVisibility(View.GONE);
                    calendar.setTimeInMillis(trip.dateInMilliSeconds + trip.timeInMilliSeconds);
                    trip.setDate(UpcomingFragment.formatter.format(calendar.getTime()));
                    trips.add(trip);
                    Objects.requireNonNull(binding.recyclerView.getAdapter()).notifyDataSetChanged();
                    if (listState != null)
                        mLayoutManager.onRestoreInstanceState(listState);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
        mTripListener = new TripListener() {
            @Override
            public void edit(String tripId) {
                Navigation.findNavController(binding.getRoot())
                        .navigate(HistoryFragmentDirections
                                .actionHistoryFragmentToAddTripFragment(getString(R.string.update_trip_title))
                                .setID(tripId).setEditMode(false));
            }

            @Override
            public void delete(Trip trip) {
                currentUserRef.child(trip.pushId).removeValue();
                FirebaseDatabase.getInstance().getReference("Notes").child(userId).child(trip.pushId).removeValue();
                Toast.makeText(getContext(), "deleting success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void startNav(Trip trip) {
            }

            @Override
            public void cancel(String id) {

            }

            @Override
            public void showNote(String id) {

            }
        }

        ;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save list state
        listState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, listState);
    }

    @Override
    public void onStart() {
        queryReference.addChildEventListener(listener);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        queryReference.removeEventListener(listener);
        trips.clear();
        Objects.requireNonNull(binding.recyclerView.getAdapter()).notifyDataSetChanged();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}