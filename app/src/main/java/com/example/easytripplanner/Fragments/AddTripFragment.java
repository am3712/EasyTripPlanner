package com.example.easytripplanner.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.easytripplanner.R;

public class AddTripFragment extends Fragment {

    private static final String ARG_PARAM = "ITEM";

    // TODO: Rename and change types of parameters
    private String mParam;

    public AddTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_trip, container, false);
    }
}