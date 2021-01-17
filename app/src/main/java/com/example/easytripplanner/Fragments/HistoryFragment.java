package com.example.easytripplanner.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.adapters.TripRecyclerViewAdapter;
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

import static com.example.easytripplanner.Fragments.UpcomingFragment.LIST_STATE_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {


    private static final String TAG = "HistoryFragment";
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm aa");

    private TripRecyclerViewAdapter viewAdapter;
    private ArrayList<Trip> trips;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private ValueEventListener listener;
    private Query queryReference;

    Parcelable listState;

    public HistoryFragment() {

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
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            recyclerView = (RecyclerView) view;
            viewAdapter = new TripRecyclerViewAdapter(getContext(), trips);
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


        queryReference = currentUserRef
                .orderByChild("status")
                .startAt(UpcomingFragment.TRIP_STATUS.CANCELED.name())
                .endAt(UpcomingFragment.TRIP_STATUS.DONE.name());

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calendar calendar = Calendar.getInstance();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        calendar.setTimeInMillis(trip.timeInMilliSeconds);
                        trip.setDate(formatter.format(calendar.getTime()));
                        trips.add(trip);
                        viewAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

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
}