package com.example.easytripplanner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Note;
import com.example.easytripplanner.utility.RemoveNote;

import java.util.List;

public class FloatAdapter extends RecyclerView.Adapter<FloatHolder> {
    Context c;
    List<Note> mData;
    private static final String TAG = "RecyleNoteAdapter";
    //private final RemoveNote removeNote;

    public FloatAdapter(Context c, List<Note> mData) {
        this.c = c;
        this.mData = mData;
        //this.removeNote = removeNote;
    }

    @NonNull
    @Override
    public FloatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FloatHolder(LayoutInflater.from(c).inflate(R.layout.note_list_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull FloatHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: item " + position + " : " + mData.get(position));
        holder.textFloat.setText(mData.get(position).text);


    }


    @Override
    public int getItemCount() {
        return mData.size();
    }
}
