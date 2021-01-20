package com.example.easytripplanner.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;

public class NoteRecyclerView extends RecyclerView.ViewHolder {
    final Button removeBuuton;
    final TextView textView;
    public NoteRecyclerView(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.textViewNote);
        removeBuuton = itemView.findViewById(R.id.btnRemov);
    }

}
