package com.example.easytripplanner.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.easytripplanner.adapters.TripRecyclerViewAdapter;
import com.example.easytripplanner.databinding.FragmentHistoryBinding;
import com.example.easytripplanner.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private static final String TAG = "HistoryFragment";
    private DatabaseReference currentUserRef;

    private ArrayList<Trip> trips;
    private ChildEventListener listener;
    private Query queryReference;

    public HistoryFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trips = new ArrayList<>();
        initQueryAndListener();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        // Set the adapter
        TripRecyclerViewAdapter viewAdapter = new TripRecyclerViewAdapter(getContext(), trips, false, currentUserRef);
        binding.getRoot().setAdapter(viewAdapter);
        return binding.getRoot();
    }

    private void initQueryAndListener() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null)
            return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        currentUserRef = null;
        if (userId != null) {
            currentUserRef = database.getReference("Users").child(userId);
            currentUserRef.keepSynced(true);
        }


        queryReference = currentUserRef
                .orderByChild("status")
                .startAt(UpcomingFragment.TRIP_STATUS.CANCELED.name())
                .endAt(UpcomingFragment.TRIP_STATUS.DONE.name());

        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Trip trip = snapshot.getValue(Trip.class);
                trips.add(trip);
                binding.getRoot().getAdapter().notifyDataSetChanged();
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
                            break;
                        }
                    }
                }
                binding.getRoot().getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
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
        binding.getRoot().getAdapter().notifyDataSetChanged();
    }
}