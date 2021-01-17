package com.example.easytripplanner.Fragments;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.utility.OnItemClickListener;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class TripRecyclerViewAdapter extends RecyclerView.Adapter<TripRecyclerViewAdapter.MyViewHolder> {

   /* CardView cardView;
    Button ButtonMore;*/

    Context context;
    ArrayList<Trip> trips;
    private final OnItemClickListener listener;


    public TripRecyclerViewAdapter(Context c, ArrayList<Trip> t, OnItemClickListener listener) {
        context = c;
        trips = t;
        this.listener = listener;
    }

    //Constuctor
     /*  public TripRecyclerViewAdapter(final View view, OnItemClickListener listener){
        this.listener = listener;
        ButtonMore = view.findViewById(R.id.btnMore);
        ButtonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(ButtonMore.getContext(), view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;

                    }
                });
                popup.inflate(R.menu.trip_menu);
              //  popup.setGravity(Gravity.RIGHT);
                try {
                    Field mFieldPopup=popup.getClass().getDeclaredField("mPopup");
                    mFieldPopup.setAccessible(true);
                    MenuPopupHelper mPopup = (MenuPopupHelper) mFieldPopup.get(popup);
                  //  mPopup.setForceShowIcon(true);
                } catch (Exception e) {

                }
                popup.show();


            }
        });
    }*/

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.trip_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tripNameView.setText(trips.get(position).name);
        holder.startPointView.setText(trips.get(position).locationFrom.Address);
        holder.endPointView.setText(trips.get(position).locationTo.Address);
        holder.endPointView.setText(trips.get(position).locationTo.Address);
        holder.dateView.setText(trips.get(position).getDate());
        holder.statusView.setText(trips.get(position).status);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(trips.get(position)));

    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public final TextView tripNameView;
        public final TextView startPointView;
        public final TextView endPointView;
        public final TextView statusView;
        public final TextView dateView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameView = itemView.findViewById(R.id.tripName);
            startPointView = itemView.findViewById(R.id.startPoint);
            endPointView = itemView.findViewById(R.id.endPoint);
            statusView = itemView.findViewById(R.id.statusView);
            dateView = itemView.findViewById(R.id.dateTextView);
        }
    }


}