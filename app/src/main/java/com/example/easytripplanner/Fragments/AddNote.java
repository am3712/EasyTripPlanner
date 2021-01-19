package com.example.easytripplanner.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_ID;


public class AddNote extends Fragment {
    private ArrayList<String> arrayList;
    private ListView listView;
    private EditText editText;
    private Button btnAdd;
    private Button btnRemov;
    private String tripId;
    Note mNote;
    DatabaseReference userRef;

    public AddNote() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //reference to user trips
        assert firebaseUser != null;
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        //Note Object
        mNote = new Note();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        listView = view.findViewById(R.id.listview);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
        listView.setAdapter(adapter);
        if (getArguments() != null && getArguments().containsKey(TRIP_ID)) {

            tripId = getArguments().getString(TRIP_ID);
            enableEditMode();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAddNote();
    }

    private void initAddNote() {
        btnAdd.setOnClickListener(v -> {

            mNote.text = editText.getText().toString();

            mNote.id = userRef.child(tripId).child("notes").push().getKey();

            userRef.child(tripId).child("notes").setValue(mNote).addOnCompleteListener(task -> {
                //Todo --> hide progress Dialog

                if (task.isSuccessful()) {
                    //      Navigation.findNavController(binding.getRoot()).navigate(AddTripFragmentDirections.actionAddTripFragmentToUpcomingFragment());

                } else {
                    //Todo show message error
                }
            });
        });

    }

    private void enableEditMode() {
        userRef.child(tripId).child("notes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mNote = snapshot.getValue(Note.class);
                fillData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fillData() {

        editText.setText(mNote.text);

    }


}