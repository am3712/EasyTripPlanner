package com.example.easytripplanner.Fragments.upcoming;

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

import com.example.easytripplanner.Fragments.TripRecyclerViewAdapter;
import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripsViewFragment extends Fragment {

    private static final String LIST_VIEW_TYPE = "LIST_TYPE";
    private int listType;

    private static final String TAG = "TripsViewFragment";

    private TripRecyclerViewAdapter viewAdapter;


    public TripsViewFragment() {

    }


    ArrayList<Trip> trips;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView recyclerView;
    public final static String LIST_STATE_KEY = "recycler_list_state";
    Parcelable listState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trips = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: Called");
        if (getArguments() != null) {
            listType = getArguments().getInt(LIST_VIEW_TYPE);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_trip, container, false);
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
        setData();
    }

    private void setData() {
        String userId = FirebaseAuth.getInstance().getUid();

        Log.i(TAG, "setData: user id: " + userId);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference currentUserRef = null;
        if (userId != null) {
            currentUserRef = database.getReference("Users").child(userId);
            currentUserRef.keepSynced(true);
            Log.i(TAG, "setData: currentUserRef: " + currentUserRef);
        }

        Query queryReference;

        if (listType == 0) {
            //get upcoming trips
            queryReference = currentUserRef
                    .orderByChild("status")
                    .equalTo("UPCOMING");
        } else {
            queryReference = currentUserRef
                    .orderByChild("status")
                    .startAt("CANCELED")
                    .endAt("DONE");
        }

        Log.i(TAG, "setData: queryReference: " + queryReference);

        queryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange: snapshot: " + snapshot);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    trips.add(dataSnapshot.getValue(Trip.class));
                    viewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.i(TAG, "setData: trips of type: " + listType + ": " + trips);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save list state
        listState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, listState);
        outState.putInt(LIST_VIEW_TYPE, listType);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Retrieve list state and list/item positions
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            listType = savedInstanceState.getInt(LIST_VIEW_TYPE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listState != null) {
            mLayoutManager.onRestoreInstanceState(listState);
        }
    }


    public static TripsViewFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(LIST_VIEW_TYPE, type);
        TripsViewFragment fragment = new TripsViewFragment();
        fragment.setArguments(args);
        return fragment;
    }


}