package com.example.easytripplanner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Note;
import com.example.easytripplanner.utility.RemoveNote;

import java.util.List;

public class RecyleNoteAdapter extends RecyclerView.Adapter<NoteRecyclerView> {
    Context c;
    List<Note> mData;
    private static final String TAG = "RecyleNoteAdapter";
    private final RemoveNote removeNote;

    public RecyleNoteAdapter(Context c, List<Note> mData, RemoveNote removeNote) {
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
        Log.i(TAG, "onBindViewHolder: item " + position + " : " + mData.get(position));
        holder.textView.setText(mData.get(position).text);
        holder.removeBuuton.setOnClickListener(v -> removeNote.remove(mData.get(position).id));

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
