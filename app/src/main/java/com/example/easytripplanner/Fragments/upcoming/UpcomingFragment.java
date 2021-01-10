package com.example.easytripplanner.Fragments.upcoming;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpcomingFragment extends Fragment {

    public UpcomingFragment(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    public UpcomingFragment() {
        trips = getStaticData();
    }


    ArrayList<Trip> trips;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView recyclerView;
    public final static String LIST_STATE_KEY = "recycler_list_state";
    Parcelable listState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(new TripRecyclerViewAdapter(getContext(), trips));
            //recyclerView.setAdapter(new TripRecyclerViewAdapter(games, item -> ((Communicator) getActivity()).openGame(item)));
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLayoutManager = recyclerView.getLayoutManager();
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
        if (savedInstanceState != null)
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listState != null) {
            mLayoutManager.onRestoreInstanceState(listState);
        }
    }


    private ArrayList<Trip> getStaticData() {
        /*ArrayList<Trip> tripArrayList = new ArrayList<>();
        tripArrayList.add(new Trip("ITI trip", "cairo", "mansoura"));
        tripArrayList.add(new Trip("pyramids trip", "fayoum", "giza"));
        tripArrayList.add(new Trip("Alex trip", "fayoum", "Alex"));
        tripArrayList.add(new Trip("ITI trip", "cairo", "mansoura"));
        tripArrayList.add(new Trip("pyramids trip", "fayoum", "giza"));
        tripArrayList.add(new Trip("Alex trip", "fayoum", "Alex"));*/
        return new ArrayList<>();
    }
}