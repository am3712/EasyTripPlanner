package com.example.easytripplanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

public class NewTripActivity extends AppCompatActivity {

    ImageButton mPickDateButton;
    ImageButton timeButton;
    Button addTripButton;
    EditText tripName;
    EditText startPointSearchView;
    EditText endPointSearchView;


    private static final int REQUEST_CODE_AUTOCOMPLETE_START_POINT = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE_END_POINT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        init();

    }

    private void init() {
        mPickDateButton = findViewById(R.id.calender_btn);
        timeButton = findViewById(R.id.timeBtn);
        addTripButton = findViewById(R.id.add_trip_btn);
        startPointSearchView = findViewById(R.id.startPointSearchView);
        endPointSearchView = findViewById(R.id.endPointSearchView);

        initClick();
    }

    private void initClick() {

        //date click
        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");

        final MaterialDatePicker materialDatePicker = materialDateBuilder.build();

        mPickDateButton.setOnClickListener(
                v -> materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));

        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                });


        //search view click (Start Point)
        startPointSearchView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) firePlaceAutocomplete(REQUEST_CODE_AUTOCOMPLETE_START_POINT);
        });
        endPointSearchView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) firePlaceAutocomplete(REQUEST_CODE_AUTOCOMPLETE_END_POINT);
        });


    }


    private void firePlaceAutocomplete(int requestCode) {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                .build(NewTripActivity.this);
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE_START_POINT) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            startPointSearchView.setText(feature.text());
            startPointSearchView.clearFocus();
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE_END_POINT) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            endPointSearchView.setText(feature.text());
            endPointSearchView.clearFocus();
        }
    }


}