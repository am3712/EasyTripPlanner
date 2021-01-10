package com.example.easytripplanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easytripplanner.models.Trip;
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
    ImageButton mPickTimeButton;
    Button mAddTripButton;
    EditText mTripName;
    EditText startPointSearchView;
    EditText endPointSearchView;
    Spinner mRepeatingSpinner;
    Spinner mTripTypeSpinner;


    FirebaseUser firebaseUser;

    Trip mCurrentTrip;


    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final String DATE_PICKER_TAG = "MATERIAL_DATE_PICKER";
    private static final String TIME_PICKER_TAG = "MATERIAL_TIME_PICKER";
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


        init();

    }

    private void init() {
        mTripName = findViewById(R.id.tripNameInput);
        mPickDateButton = findViewById(R.id.calender_btn);
        mPickTimeButton = findViewById(R.id.timeBtn);
        mAddTripButton = findViewById(R.id.add_trip_btn);
        startPointSearchView = findViewById(R.id.startPointSearchView);
        endPointSearchView = findViewById(R.id.endPointSearchView);
        mRepeatingSpinner = findViewById(R.id.repeating_spinner);
        mTripTypeSpinner = findViewById(R.id.trip_type);

        initComponent();
    }

    private void initComponent() {

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

    private void initAddTrip() {
        mAddTripButton.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

            //TODO validate trip attributes (make sure no empty cells)


            mCurrentTrip.name = mTripName.getText().toString().trim();
            mCurrentTrip.type = (String) mTripTypeSpinner.getSelectedItem();
            mCurrentTrip.repeating = (String) mRepeatingSpinner.getSelectedItem();
            mCurrentTrip.status = "UPCOMING";


            //insert trip to specific user
            mCurrentTrip.pushId = userRef.push().getKey();
            userRef.child(mCurrentTrip.pushId).setValue(mCurrentTrip).addOnSuccessListener(aVoid -> {
                Toast.makeText(NewTripActivity.this, "Trip Added Successfully", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "initAddTrip: Trip Added Successfully");
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
                            Log.i(TAG, "initTimePicker: time: " + mCurrentTrip.time);
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
                });
    }


    private void firePlaceAutocomplete() {
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
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
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
    }


}