package com.example.easytripplanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.models.Note;
import com.example.easytripplanner.models.TripLocation;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewTripActivity extends AppCompatActivity {


    ImageButton mPickDateButton;
    ImageButton timeButton;
    Button addTripButton, addTripNote;
    EditText tripName;
    ImageButton mPickTimeButton;
    Button mAddTripButton;
    EditText mTripName;
    EditText startPointSearchView;
    EditText endPointSearchView;
    Spinner mRepeatingSpinner;
    Spinner mTripTypeSpinner;
    ConstraintLayout myLayout;
    TextView dateView;
    TextView timeView;
    TextView editNote;


    FirebaseUser firebaseUser;

    Trip mCurrentTrip;
    Note mNote;

    private static final int LOCATION_REQUEST_CODE = 0;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String DATE_PICKER_TAG = "MATERIAL_DATE_PICKER";
    private static final String TAG = "NewTripActivity";

    boolean startPointClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        //firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        //Trip Object
        mCurrentTrip = new Trip();

        //Note Object
        mNote=new Note();


        init();

    }

    private void init() {
        mTripName = findViewById(R.id.tripNameInput);
        mPickDateButton = findViewById(R.id.calender_btn);
        timeButton = findViewById(R.id.timeBtn);
        addTripButton = findViewById(R.id.add_trip_btn);
        addTripNote = findViewById(R.id.btnAddNote);
        mPickTimeButton = findViewById(R.id.timeBtn);
        mAddTripButton = findViewById(R.id.add_trip_btn);
        startPointSearchView = findViewById(R.id.startPointSearchView);
        endPointSearchView = findViewById(R.id.endPointSearchView);
        mRepeatingSpinner = findViewById(R.id.repeating_spinner);
        mTripTypeSpinner = findViewById(R.id.trip_type);
        myLayout = findViewById(R.id.my_layout);
        dateView = findViewById(R.id.dateTextView);
        timeView = findViewById(R.id.timeTextView);
        editNote=findViewById(R.id.editNote);

        initComponent();
    }

    private void initComponent() {
        initTripName();
        initDatePicker();
        initTimePicker();
        initAddTrip();

        //search view click (Start Point)
        startPointSearchView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                startPointClicked = true;
                firePlaceAutocomplete();
            }
        });
        endPointSearchView.setOnFocusChangeListener((v, hasFocus) -> {
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

    private void initAddTrip() {
        mAddTripButton.setOnClickListener(v -> {

            //TODO validate trip attributes (make sure no empty cells)
            if (!validateInput()) {
                return;
            }
            //TODO show progress Dialog here


            addTripNote.setOnClickListener(v1 -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewTripActivity.this);
                final View noteDialog = getLayoutInflater().inflate(R.layout.note_dialog, null);
                builder.setView(noteDialog);
                // builder.setIcon(R.drawable.ic_baseline_note_add_24);
                builder.setTitle("Add Note");

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = noteDialog.findViewById(R.id.editNote);
                        Toast.makeText(NewTripActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(NewTripActivity.this, "you cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            });


            // now handle the positive button click from the
            // material design date picker
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            mCurrentTrip.type = (String) mTripTypeSpinner.getSelectedItem();
            mCurrentTrip.repeating = (String) mRepeatingSpinner.getSelectedItem();
            mCurrentTrip.status = "UPCOMING";


            //insert trip to specific user
            mCurrentTrip.pushId = userRef.push().getKey();
            userRef.child(mCurrentTrip.pushId).setValue(mCurrentTrip).addOnCompleteListener(task -> {
                //Todo --> hide progress Dialog

                if (task.isSuccessful()) {
                    Toast.makeText(NewTripActivity.this, "Trip Added Successfully", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "initAddTrip: Trip Added Successfully");
                    //finish activity
                    finish();

                } else {
                    //Todo show message error
                }
            });
            Log.i(TAG, "initAddTrip: " + mCurrentTrip.toString());

        });
    }

    @SuppressLint("SimpleDateFormat")
    private void initTimePicker() {
        mPickTimeButton.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(
                    this,
                    (view11, hour, minute) -> {
                        Date date = null;
                        try {
                            date = new SimpleDateFormat("HH:mm").parse(hour + ":" + minute);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (date != null) {
                            mCurrentTrip.time = new SimpleDateFormat("hh:mm aa").format(date);
                            timeView.setText(mCurrentTrip.time);
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
        CalendarConstraints.DateValidator dateValidator = DateValidatorPointForward.from(System.currentTimeMillis());
        final MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT TRIP DATE")
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(dateValidator).build())
                .build();
        mPickDateButton.setOnClickListener(v -> materialDatePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG));
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    mCurrentTrip.date = materialDatePicker.getHeaderText();
                    dateView.setText(mCurrentTrip.date);
                });
    }


    private void firePlaceAutocomplete() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(NewTripActivity.this, permissions, LOCATION_REQUEST_CODE);
            return;
        }
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                .build(NewTripActivity.this);
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        startPointSearchView.setText(feature.text());
                    } else {
                        mCurrentTrip.locationTo = locationSrc;
                        endPointSearchView.setText(feature.text());
                    }
                    Log.i(TAG, "onActivityResult: Location: " + locationSrc);
                    Log.i(TAG, "onActivityResult: mCurrentTrip.locationFrom: " + mCurrentTrip.locationFrom);
                    Log.i(TAG, "onActivityResult: mCurrentTrip.locationTo: " + mCurrentTrip.locationTo);
                }
            }
            myLayout.requestFocus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            Log.i(TAG, "onActivityResult: LOCATION_REQUEST_CODE : " + LOCATION_REQUEST_CODE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onActivityResult: LOCATION_REQUEST_CODE : PERMISSION_GRANTED");
                firePlaceAutocomplete();
            }
        }
    }


    private boolean validateInput() {
        if (mCurrentTrip.name == null || mCurrentTrip.name.isEmpty())
            Toast.makeText(this, "Trip Name Is Empty!!", Toast.LENGTH_SHORT).show();
        else if (mCurrentTrip.locationFrom == null)
            Toast.makeText(this, "Start Point Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mCurrentTrip.locationTo == null)
            Toast.makeText(this, "end Point Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mCurrentTrip.time == null || mCurrentTrip.time.isEmpty())
            Toast.makeText(this, "Time Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mCurrentTrip.date == null || mCurrentTrip.date.isEmpty())
            Toast.makeText(this, "Date Not Specified!!", Toast.LENGTH_SHORT).show();
        else
            return true;

        return false;
    }

}