package com.example.easytripplanner.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.utility.TripListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class TripRecyclerViewAdapter extends RecyclerView.Adapter<TripRecyclerViewAdapter.MyViewHolder> {


    Context context;
    ArrayList<Trip> trips;
    boolean isUpcomingList;
    private final TripListener tripListener;

    public TripRecyclerViewAdapter(Context c, ArrayList<Trip> t, boolean isUpcomingList, TripListener tripListener) {
        context = c;
        trips = t;
        this.isUpcomingList = isUpcomingList;
        this.tripListener = tripListener;
    }

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
        holder.btnMore.setOnClickListener(v -> showMenu(holder.btnMore, trips.get(position)));
        if (isUpcomingList)
            holder.mStartBtn.setOnClickListener(v -> tripListener.startNav(trips.get(position)));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public final TextView tripNameView;
        public final TextView startPointView;
        public final TextView endPointView;
        public final TextView statusView;
        public final TextView dateView;
        public final Button btnMore;
        public final Button mStartBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameView = itemView.findViewById(R.id.tripName);
            startPointView = itemView.findViewById(R.id.startPoint);
            endPointView = itemView.findViewById(R.id.endPoint);
            statusView = itemView.findViewById(R.id.statusView);
            dateView = itemView.findViewById(R.id.dateTextView);
            btnMore = itemView.findViewById(R.id.btnMore);
            mStartBtn = itemView.findViewById(R.id.btnStart);
            if (!isUpcomingList)
                mStartBtn.setVisibility(View.GONE);

        }
    }

    @SuppressLint("NonConstantResourceId")
    private void showMenu(View v, Trip trip) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        setForceShowIcon(popupMenu);
        popupMenu.getMenuInflater().inflate(R.menu.trip_menu, popupMenu.getMenu());
        if (!isUpcomingList)
            popupMenu.getMenu().removeItem(R.id.editTrip);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.editTrip:
                    tripListener.editItem(trip.pushId);
                    return true;
                case R.id.deleteTrip:
                    tripListener.deleteItem(trip);
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }


    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = null;
                    if (menuPopupHelper != null) {
                        classPopupHelper = Class.forName(menuPopupHelper
                                .getClass().getName());
                    }
                    Method setForceIcons = null;
                    if (classPopupHelper != null) {
                        setForceIcons = classPopupHelper.getMethod(
                                "setForceShowIcon", boolean.class);
                    }
                    if (setForceIcons != null) {
                        setForceIcons.invoke(menuPopupHelper, true);
                    }
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}