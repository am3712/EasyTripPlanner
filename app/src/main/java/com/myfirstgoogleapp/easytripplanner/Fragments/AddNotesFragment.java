package com.myfirstgoogleapp.easytripplanner.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirstgoogleapp.easytripplanner.R;
import com.myfirstgoogleapp.easytripplanner.adapters.RecyclerNoteAdapter;
import com.myfirstgoogleapp.easytripplanner.models.Note;
import com.myfirstgoogleapp.easytripplanner.utility.RemoveNote;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

public class AddNotesFragment extends Fragment {

    private List<Note> arrayList;
    private EditText editText;
    private Button btnAdd;
    private String tripId;
    //private ArrayAdapter<String> adapter;
    private ChildEventListener notesListener;
    DatabaseReference userNotesRef;
    private RecyclerNoteAdapter recyclerNoteAdapter;
    RemoveNote removeNote;

    public AddNotesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removeNote = noteID -> {
            userNotesRef.child(noteID).removeValue();
            Toast.makeText(getContext(), "deleting success", Toast.LENGTH_SHORT).show();
        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        arrayList = new ArrayList<>();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_notes, container, false);
        editText = view.findViewById(R.id.editText);
        btnAdd = view.findViewById(R.id.btnAdd);
        RecyclerView recyclerView = view.findViewById(R.id.listview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerNoteAdapter = new RecyclerNoteAdapter(getContext(), arrayList, removeNote);
        recyclerView.setAdapter(recyclerNoteAdapter);

        initAddNote();

        return view;
    }

    private void initAddNote() {
        btnAdd.setOnClickListener(v -> {
            String text = editText.getText().toString();
            String id = userNotesRef.push().getKey();
            assert id != null;
            userNotesRef.child(id).setValue(new Note(text, false, id));
            Toast.makeText(getContext(), "Note is add successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null)
            return;

        if (getArguments() != null) {
            tripId = AddNotesFragmentArgs.fromBundle(getArguments()).getID();
            Timber.i("onViewCreated: Trip id :%s", tripId);
        }

        Timber.i("onViewCreated: User Id: %s", userId);
        userNotesRef = FirebaseDatabase.getInstance().getReference("Notes").child(userId).child(tripId);


        initTripLis();
    }

    private void initTripLis() {
        notesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Note note = snapshot.getValue(Note.class);
                if (note != null) {
                    Timber.i("onChildAdded: note text : %s", note.text);
                    arrayList.add(note);
                    recyclerNoteAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String id = snapshot.child("id").getValue(String.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    arrayList.removeIf(note -> note.id.equals(id));
                } else {
                    for (Iterator<Note> iterator = arrayList.iterator(); iterator.hasNext(); ) {
                        if (iterator.next().id.equals(id)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                recyclerNoteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        userNotesRef.addChildEventListener(notesListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.i("onStop: removeEventListener");
        userNotesRef.removeEventListener(notesListener);
    }
}