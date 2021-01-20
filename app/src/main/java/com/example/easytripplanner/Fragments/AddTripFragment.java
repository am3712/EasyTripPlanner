package com.example.easytripplanner.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.FragmentAddTripBinding;
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

import timber.log.Timber;


public class AddTripFragment extends Fragment {

    private ImageButton mPickDateButton;
    private Button mAddTripNote;
    private ImageButton mPickTimeButton;
    private Button mSaveTripButton;
    private EditText mTripName;
    private EditText mStartPointEditText;
    private EditText mEndPointEditText;
    private Spinner mRepeatingSpinner;
    private Spinner mTripTypeSpinner;
    private ConstraintLayout myLayout;
    private TextView mDateTextView;
    private TextView mTimeTextView;

    private String tripId;
    private FragmentAddTripBinding binding;

    Trip mCurrentTrip;
    Note mNote;
    private long dateInMilliseconds;
    private long timeInMilliseconds;
    private final DatabaseReference userRef;

    private static final int LOCATION_REQUEST_CODE = 0;
    private static final String DATE_PICKER_TAG = "MATERIAL_DATE_PICKER";

    boolean startPointClicked;
    private Context context;

    private int saveMode;

    private final ActivityResultLauncher<Intent> autoCompletePlaceActivityResultLauncher;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher;


    public AddTripFragment() {

        autoCompletePlaceActivityResultLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
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
                        });


        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            if (result.get(Manifest.permission.ACCESS_FINE_LOCATION)
                                    && result.get(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                Timber.i("AddTripFragment: requestPermissionLauncher");
                                firePlaceAutocomplete();
                            }
                        });


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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTripBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setMode();

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
        if (saveMode == 1)
            mSaveTripButton.setText(R.string.save_edit_btn);
        else
            mSaveTripButton.setText(R.string.save_update_btn);
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
        mSaveTripButton.setOnClickListener(v -> {

            //TODO validate trip attributes (make sure no empty cells)
            if (!validateInput()) {
                return;
            }
            //TODO show progress Dialog here


            // now handle the positive button click from the
            // material design date picker

            mCurrentTrip.type = (String) mTripTypeSpinner.getSelectedItem();
            mCurrentTrip.repeating = (String) mRepeatingSpinner.getSelectedItem();
            mCurrentTrip.status = UpcomingFragment.TRIP_STATUS.UPCOMING.name();
            mCurrentTrip.timeInMilliSeconds = timeInMilliseconds + dateInMilliseconds;

            if (mCurrentTrip.timeInMilliSeconds <= System.currentTimeMillis()) {
                Toast.makeText(context, "change time ,you can not add past date!!", Toast.LENGTH_SHORT).show();
                return;
            }
            //insert trip to specific user
            if (saveMode == 0)
                mCurrentTrip.pushId = userRef.push().getKey();

            if (mCurrentTrip.pushId != null) {
                userRef.child(mCurrentTrip.pushId).setValue(mCurrentTrip).addOnCompleteListener(task -> {
                    //Todo --> hide progress Dialog

                    if (task.isSuccessful()) {
                        String message = (saveMode != 0) ? (saveMode == 1) ? getString(R.string.trip_edit_success)
                                : getString(R.string.trip_updated_success)
                                : getString(R.string.Trip_added_successfully);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(binding.getRoot()).navigate(AddTripFragmentDirections.actionAddTripFragmentToUpcomingFragment());

                    } else {
                        //Todo show message errorsz
                    }
                });
            }
        });
    }

    private void setComponentsAction() {
        initTripName();
        initDatePicker();
        initTimePicker();
        initAddTrip();

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
        mPickTimeButton = binding.timeBtn;
        mSaveTripButton = binding.addTripBtn;
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
        MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT TRIP DATE")
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(dateValidator).build())
                .build();
        mPickDateButton.setOnClickListener(v -> materialDatePicker.show(getParentFragmentManager(), DATE_PICKER_TAG));
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    dateInMilliseconds = (long) materialDatePicker.getSelection();
                    mDateTextView.setText(materialDatePicker.getHeaderText());
                });

    }

    private void firePlaceAutocomplete() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Timber.i("firePlaceAutocomplete: PERMISSION_GRANTED ");
            performAutoCompletePlaceAction();
        } else {
            // You can directly ask for the permission.
            Timber.i("firePlaceAutocomplete: PERMISSION_DENIED ");
            requestPermissionLauncher.launch(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION});
        }


    }

    private void performAutoCompletePlaceAction() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                .build(getActivity());
        autoCompletePlaceActivityResultLauncher.launch(intent);
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

    private void setMode() {
        assert getArguments() != null;
        tripId = AddTripFragmentArgs.fromBundle(getArguments()).getID();
        if (!tripId.equals("EMPTY")) {
            boolean isEdit = AddTripFragmentArgs.fromBundle(getArguments()).getEditMode();
            Timber.i("onViewCreated: isEdit: %s", isEdit);
            if (isEdit)
                saveMode = 1;
            else
                saveMode = 2;

            enableEditMode();
        }
    }
}