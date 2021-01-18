package com.example.easytripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.FragmentNewTripBinding;
import com.example.easytripplanner.models.Note;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.models.TripLocation;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_ID;


public class NewTripFragment extends Fragment {

    private ImageButton mPickDateButton;
    private Button mAddTripNote;
    private ImageButton mPickTimeButton;
    private Button mAddTripButton;
    private EditText mTripName;
    private EditText mStartPointEditText;
    private EditText mEndPointEditText;
    private Spinner mRepeatingSpinner;
    private Spinner mTripTypeSpinner;
    private ConstraintLayout myLayout;
    private TextView mDateTextView;
    private TextView mTimeTextView;

    private String tripId;
    private FragmentNewTripBinding binding;

    Trip mCurrentTrip;
    Note mNote;
    private long dateInMilliseconds;
    private long timeInMilliseconds;
    private DatabaseReference userRef;

    private static final int LOCATION_REQUEST_CODE = 0;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String DATE_PICKER_TAG = "MATERIAL_DATE_PICKER";
    private static final String TAG = "NewTripActivity";

    boolean startPointClicked;
    private Context context;

    public NewTripFragment() {



        //firebase
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //reference to user trips
        assert firebaseUser != null;
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        //Trip Object
        mCurrentTrip = new Trip();

        //Note Object
        mNote = new Note();
    }


    public static NewTripFragment newInstance(String param1) {
        NewTripFragment fragment = new NewTripFragment();
        Bundle args = new Bundle();

       /* if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(TRIP_ID)) {
            tripId = getIntent().getExtras().getString(TRIP_ID);
            enableEditMode();*/

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //tripId = getArguments().getString(TRIP_ID);

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewTripBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initComponents();
        if (getArguments() != null && getArguments().containsKey(TRIP_ID)) {

            tripId = getArguments().getString(TRIP_ID);
            enableEditMode();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(context, getString(R.string.access_token));

        setComponentsAction();
    }

    private void enableEditMode() {
        userRef.child(tripId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mCurrentTrip = snapshot.getValue(Trip.class);
                fillData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mAddTripButton.setText(R.string.save_edit_btn_txt);
    }

    private void fillData() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mCurrentTrip.timeInMilliSeconds);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");

        mTripName.setText(mCurrentTrip.name);
        mStartPointEditText.setText(mCurrentTrip.locationFrom.Address);
        mEndPointEditText.setText(mCurrentTrip.locationTo.Address);
        mDateTextView.setText(dateFormat.format(calendar.getTime()));
        mTimeTextView.setText(timeFormat.format(calendar.getTime()));
    }


    private void initAddTrip() {
        mAddTripButton.setOnClickListener(v -> {

            //TODO validate trip attributes (make sure no empty cells)
            if (!validateInput()) {
                return;
            }
            //TODO show progress Dialog here


            // now handle the positive button click from the
            // material design date picker

            mCurrentTrip.type = (String) mTripTypeSpinner.getSelectedItem();
            mCurrentTrip.repeating = (String) mRepeatingSpinner.getSelectedItem();
            mCurrentTrip.status = "UPCOMING";
            mCurrentTrip.timeInMilliSeconds = timeInMilliseconds + dateInMilliseconds;

            //insert trip to specific user
            if (tripId == null)
                mCurrentTrip.pushId = userRef.push().getKey();

            if (mCurrentTrip.pushId != null) {
                userRef.child(mCurrentTrip.pushId).setValue(mCurrentTrip).addOnCompleteListener(task -> {
                    //Todo --> hide progress Dialog

                    if (task.isSuccessful()) {
                        String message = (tripId == null) ? "Trip Added Successfully" : "Trip Edit Success";

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        //finish activity
                        // finishAndRemoveTask();
                        getActivity().finish();
                    } else {
                        //Todo show message error
                    }
                });
            }
        });
    }

    private void initAddNote() {
        mAddTripNote.setOnClickListener(v1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final View noteDialog = getLayoutInflater().inflate(R.layout.note_dialog, null);
            builder.setView(noteDialog);
            // builder.setIcon(R.drawable.ic_baseline_note_add_24);
            builder.setTitle("Add Note");

            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText editText = noteDialog.findViewById(R.id.editNote);
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(context, "you cancelled", Toast.LENGTH_SHORT).show();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void setComponentsAction() {
        initTripName();
        initDatePicker();
        initTimePicker();
        initAddTrip();
        initAddNote();

        //search view click (Start Point)
        mStartPointEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                startPointClicked = true;
                firePlaceAutocomplete();
            }
        });
        mEndPointEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                startPointClicked = false;
                firePlaceAutocomplete();
            }
        });


    }

    private void initTripName() {
        mTripName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCurrentTrip.name = s.toString();
            }
        });
    }

    private void initComponents() {
        mTripName = binding.tripNameInput;
        mPickDateButton = binding.calenderBtn;
        mAddTripNote = binding.btnAddNote;
        mPickTimeButton = binding.timeBtn;
        mAddTripButton = binding.addTripBtn;
        mStartPointEditText = binding.startPointSearchView;
        mEndPointEditText = binding.endPointSearchView;
        mRepeatingSpinner = binding.repeatingSpinner;
        mTripTypeSpinner = binding.tripType;
        myLayout = binding.myLayout;
        mDateTextView = binding.dateTextView;
        mTimeTextView = binding.timeTextView;
        context = getActivity();
        setComponentsAction();
    }

    @SuppressLint("SimpleDateFormat")
    private void initTimePicker() {
        mPickTimeButton.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(
                    context,
                    (view11, hour, minute) -> {
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("HH:mm").parse(hour + ":" + minute);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (date != null) {
                            mTimeTextView.setText(new SimpleDateFormat("hh:mm aa").format(date));
                            timeInMilliseconds = date.getTime();
                        }
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
            ).show();
        });
    }

    private void initDatePicker() {

        //range of date
        CalendarConstraints.DateValidator dateValidator = DateValidatorPointForward.from((long) (System.currentTimeMillis() - 8.64e+7));
        final MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT TRIP DATE")
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(dateValidator).build())
                .build();
        mPickDateButton.setOnClickListener(v -> materialDatePicker.show(getFragmentManager(), DATE_PICKER_TAG));
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    dateInMilliseconds = (long) materialDatePicker.getSelection();
                    mDateTextView.setText(materialDatePicker.getHeaderText());
                });

    }

    private void firePlaceAutocomplete() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_REQUEST_CODE);
            return;
        }
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                .build(getActivity());
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == Activity.RESULT_OK) {
                CarmenFeature feature = PlaceAutocomplete.getPlace(data);
                Point point = (Point) feature.geometry();
                TripLocation locationSrc = null;
                if (point != null) {
                    locationSrc = new TripLocation(
                            feature.text(),
                            point.latitude(),
                            point.longitude());
                    if (startPointClicked) {
                        mCurrentTrip.locationFrom = locationSrc;
                        mStartPointEditText.setText(feature.text());
                    } else {
                        mCurrentTrip.locationTo = locationSrc;
                        mEndPointEditText.setText(feature.text());
                    }
                }
            }
            myLayout.requestFocus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                firePlaceAutocomplete();
            }
        }
    }

    private boolean validateInput() {
        if (mCurrentTrip.name == null || mCurrentTrip.name.isEmpty())
            Toast.makeText(context, "Trip Name Is Empty!!", Toast.LENGTH_SHORT).show();
        else if (mCurrentTrip.locationFrom == null)
            Toast.makeText(context, "Start Point Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mCurrentTrip.locationTo == null)
            Toast.makeText(context, "end Point Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mTimeTextView.getText().toString().isEmpty())
            Toast.makeText(context, "Time Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mDateTextView.getText().toString().isEmpty())
            Toast.makeText(context, "Date Not Specified!!", Toast.LENGTH_SHORT).show();
        else
            return true;

        return false;
    }
}