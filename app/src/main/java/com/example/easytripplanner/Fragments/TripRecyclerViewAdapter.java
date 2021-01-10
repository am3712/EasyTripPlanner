package com.example.easytripplanner.Fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Trip;

import java.util.ArrayList;

public class TripRecyclerViewAdapter extends RecyclerView.Adapter<TripRecyclerViewAdapter.MyViewHolder> {
    Context context;
    ArrayList<Trip> trips;

    public TripRecyclerViewAdapter(Context c, ArrayList<Trip> t) {
        context = c;
        trips = t;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.trip_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textViewTripName.setText(trips.get(position).name);
        holder.textViewStartPoint.setText(trips.get(position).locationFrom.Address);
        holder.textViewEndPoint.setText(trips.get(position).locationTo.Address);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewTripName;
        public final TextView textViewStartPoint;
        public final TextView textViewEndPoint;
        public final View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            textViewTripName = itemView.findViewById(R.id.tripName);
            textViewStartPoint = itemView.findViewById(R.id.startPoint);
            textViewEndPoint = itemView.findViewById(R.id.endPoint);
        }
    }
}