package com.example.easytripplanner.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Note;

import java.util.List;

public class FloatAdapter extends RecyclerView.Adapter<FloatAdapter.FloatHolder> {
    Context c;
    List<Note> mData;
    private static final String TAG = "RecyclerNoteAdapter";
    //private final RemoveNote removeNote;

    public FloatAdapter(Context c, List<Note> mData) {
        this.c = c;
        this.mData = mData;
        //this.removeNote = removeNote;
    }

    @NonNull
    @Override
    public FloatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FloatHolder(LayoutInflater.from(c).inflate(R.layout.floating_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull FloatHolder holder, int position) {
        holder.textFloat.setText(mData.get(position).text);
        holder.checkBox.setChecked(mData.get(position).checked);

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.textFloat.setPaintFlags(holder.textFloat.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.textFloat.setPaintFlags(holder.textFloat.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    static class FloatHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView textFloat;

        public FloatHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.complete_checkbox);
            textFloat = itemView.findViewById(R.id.title_text);
        }

    }
}
