package com.myfirstgoogleapp.easytripplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstgoogleapp.easytripplanner.R;
import com.myfirstgoogleapp.easytripplanner.models.Note;
import com.myfirstgoogleapp.easytripplanner.utility.RemoveNote;

import java.util.List;

import timber.log.Timber;

public class RecyclerNoteAdapter extends RecyclerView.Adapter<RecyclerNoteAdapter.NoteRecyclerView> {
    Context c;
    List<Note> mData;
    private final RemoveNote removeNote;

    public RecyclerNoteAdapter(Context c, List<Note> mData, RemoveNote removeNote) {
        this.c = c;
        this.mData = mData;
        this.removeNote = removeNote;
    }

    @NonNull
    @Override
    public NoteRecyclerView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteRecyclerView(LayoutInflater.from(c).inflate(R.layout.note_list_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull NoteRecyclerView holder, int position) {
        Timber.i("onBindViewHolder: item " + position + " : " + mData.get(position));
        holder.textView.setText(mData.get(position).text);

        holder.removeButton.setOnClickListener(v -> removeNote.remove(mData.get(position).id));

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    static class NoteRecyclerView extends RecyclerView.ViewHolder {
        final Button removeButton;
        final TextView textView;

        public NoteRecyclerView(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewNote);
            removeButton = itemView.findViewById(R.id.btnRemov);
        }

    }
}
