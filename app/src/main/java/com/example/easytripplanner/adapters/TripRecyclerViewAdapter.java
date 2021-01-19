package com.example.easytripplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.Fragments.LoginFragmentDirections;
import com.example.easytripplanner.Fragments.UpcomingFragment;
import com.example.easytripplanner.Fragments.UpcomingFragmentDirections;
import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Trip;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class TripRecyclerViewAdapter extends RecyclerView.Adapter<TripRecyclerViewAdapter.MyViewHolder> {

    private final DatabaseReference currentUserRef;

    Context context;
    ArrayList<Trip> trips;
    boolean isUpcomingList;

    public TripRecyclerViewAdapter(Context c, ArrayList<Trip> t, boolean isUpcomingList, DatabaseReference currentUserRef) {
        context = c;
        trips = t;
        this.isUpcomingList = isUpcomingList;
        this.currentUserRef = currentUserRef;
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
        holder.btnMore.setOnClickListener(v -> showMenu(holder.btnMore, trips.get(position).pushId));
        //if (isUpcomingList)
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

    private void showMenu(View v, String tripId) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.getMenuInflater().inflate(R.menu.trip_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit:
                    Toast.makeText(context, "edit", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.del:
                    currentUserRef.child(tripId).removeValue((error, ref) ->
                            Toast.makeText(context, "deleting success", Toast.LENGTH_SHORT).show());
                    return true;
                case R.id.add_note:
                    Navigation.findNavController(v).navigate(UpcomingFragmentDirections.actionUpcomingFragmentToAddNote(tripId));
                    Toast.makeText(context, "deleting success", Toast.LENGTH_SHORT).show();

            }
            return false;
        });
        popupMenu.show();
    }

}