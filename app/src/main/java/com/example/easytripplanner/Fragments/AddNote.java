package com.example.easytripplanner.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytripplanner.R;
import com.example.easytripplanner.adapters.RecyclerNoteAdapter;
import com.example.easytripplanner.models.Note;
import com.example.easytripplanner.utility.RemoveNote;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;


public class AddNote extends Fragment {
    private List<Note> arrayList;
    private ListView listView;
    private EditText editText;
    private Button btnAdd;
    private Button btnRemov;
    private String tripId;
    //private ArrayAdapter<String> adapter;
    private ChildEventListener notesListener;
    private RecyclerView recyclerView;
    private static final String TAG = "AddNote";
    DatabaseReference userRef;
    private RecyclerNoteAdapter recyclerNoteAdapter;
    RemoveNote removeNote;

    public AddNote() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //reference to user trips
        assert firebaseUser != null;
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        removeNote = new RemoveNote() {
            @Override
            public void remove(String noteID) {
                userRef.child(tripId).child("notes").child(noteID).removeValue((error, ref) ->
                        Toast.makeText(getContext(), "deleting success", Toast.LENGTH_SHORT).show());

            }
        };
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = AddNoteArgs.fromBundle(getArguments()).getID();
            Timber.i("onViewCreated: Trip id :" + tripId);
        }
        initTripLis();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        arrayList = new ArrayList<>();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_note, container, false);
        editText = view.findViewById(R.id.editText);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnRemov = view.findViewById(R.id.btnRemov);
        recyclerView = view.findViewById(R.id.listview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerNoteAdapter = new RecyclerNoteAdapter(getContext(), arrayList,removeNote);
        recyclerView.setAdapter(recyclerNoteAdapter);

        /*adapter = new ArrayAdapter<String>(getContext(),
                R.layout.note_list_item, R.id.textViewNote, arrayList);
        listView.setAdapter(adapter);*/

        initAddNote();

        return view;
    }

    private void initAddNote() {
        btnAdd.setOnClickListener(v -> {

            String text = editText.getText().toString();

            String id = userRef.child(tripId).child("notes").push().getKey();
            assert id != null;
            userRef.child(tripId).child("notes").child(id).setValue(new Note(text, false, id)).addOnCompleteListener(task -> {
                //Todo --> hide progress Dialog

                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Note is add successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Note is not added successfully", Toast.LENGTH_SHORT).show();
                }
            });
        });

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
        userRef.child(tripId).child("notes").addChildEventListener(notesListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: removeEventListener");
        userRef.child(tripId).child("notes").removeEventListener(notesListener);
    }
}