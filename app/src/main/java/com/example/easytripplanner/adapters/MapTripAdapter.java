package com.example.easytripplanner.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.utility.MapCameraListener;
import com.example.easytripplanner.utility.Common;

import java.util.List;

import timber.log.Timber;

public class MapTripAdapter extends RecyclerView.Adapter<MapTripAdapter.MapTripHolder> {
    Context c;
    List<String> mData;
    MapCameraListener mapCameraListener;
    //private final RemoveNote removeNote;

    public MapTripAdapter(Context c, List<String> mData, MapCameraListener listener) {
        this.c = c;
        this.mData = mData;
        mapCameraListener = listener;
        Timber.i("created");
    }

    @NonNull
    @Override
    public MapTripHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MapTripHolder(LayoutInflater.from(c).inflate(R.layout.map_list_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MapTripHolder holder, int position) {
        holder.tripName.setText(mData.get(position));
        holder.color.setBackgroundColor(Color.parseColor(Common.MAPS_TRIPS_COLORS[position % 11]));
        Timber.i("Trip name %s inserted", mData.get(position));
        holder.itemView.setOnClickListener(v -> mapCameraListener.focus(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    static class MapTripHolder extends RecyclerView.ViewHolder {

        final TextView tripName;
        final FrameLayout color;

        public MapTripHolder(@NonNull View itemView) {
            super(itemView);
            tripName = itemView.findViewById(R.id.mapTripName);
            color = itemView.findViewById(R.id.colorContainer);
        }
    }
}
