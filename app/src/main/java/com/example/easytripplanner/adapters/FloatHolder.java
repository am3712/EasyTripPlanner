package com.example.easytripplanner.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;

public class FloatHolder extends RecyclerView.ViewHolder {
    final CheckBox checkBox;
    final TextView textFloat;

    public FloatHolder(@NonNull View itemView) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.checkBox);
        textFloat = itemView.findViewById(R.id.text_float);
    }

}
