package com.example.easytripplanner.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.example.easytripplanner.Fragments.UpcomingFragment.formatter;


public class AddTripFragment extends Fragment {

    private EditText mPickDateEditText;
    private EditText mPickTimeEditText;
    private Button mSaveTripButton;
    private EditText mTripName;
    private EditText mStartPointEditText;
    private EditText mEndPointEditText;
    private Spinner mRepeatingSpinner;
    private Spinner mTripTypeSpinner;
    private ConstraintLayout myLayout;
    private List<String> repeatingOptions;


    private String tripId;
    private FragmentAddTripBinding binding;

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm aa");

    Trip mTrip;
    Note mNote;
    private final DatabaseReference userRef;

    private static final String DATE_PICKER_TAG = "MATERIAL_DATE_PICKER";

    boolean startPointClicked;
    private Context context;

    private int saveMode;

    private final ActivityResultLauncher<Intent> autoCompletePlaceActivityResultLauncher;


    public AddTripFragment() {
        autoCompletePlaceActivityResultLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                CarmenFeature feature = null;
                                if (data != null) {
                                    feature = PlaceAutocomplete.getPlace(data);
                                }
                                Point point = null;
                                if (feature != null) {
                                    point = (Point) feature.geometry();
                                }
                                TripLocation locationSrc = null;
                                if (point != null) {
                                    locationSrc = new TripLocation(
                                            feature.text(),
                                            point.latitude(),
                                            point.longitude());
                                    if (startPointClicked) {
                                        mTrip.locationFrom = locationSrc;
                                        mStartPointEditText.setText(feature.text());
                                    } else {
                                        mTrip.locationTo = locationSrc;
                                        mEndPointEditText.setText(feature.text());
                                    }
                                }
                            }
                            myLayout.requestFocus();
                        });


        //firebase
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //reference to user trips
        assert firebaseUser != null;
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        //Trip Object
        mTrip = new Trip();

        //Note Object
        mNote = new Note();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repeatingOptions = Arrays.asList(getResources().getStringArray(R.array.repeating_options));
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
                mTrip = snapshot.getValue(Trip.class);
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

        calendar.setTimeInMillis(mTrip.dateInMilliSeconds);
        String date = DATE_FORMAT.format(calendar.getTime());

        calendar.setTimeInMillis(mTrip.timeInMilliSeconds);
        String time = TIME_FORMAT.format(calendar.getTime());

        Timber.i("date: %s", date);
        Timber.i("time: %s", time);


        mTripName.setText(mTrip.name);
        mStartPointEditText.setText(mTrip.locationFrom.Address);
        mEndPointEditText.setText(mTrip.locationTo.Address);
        mPickDateEditText.setText(date);
        mPickTimeEditText.setText(time);
        mRepeatingSpinner.setSelection(repeatingOptions.indexOf(mTrip.repeating));
    }


    private void initAddTrip() {
        mSaveTripButton.setOnClickListener(v -> {

            //TODO validate trip attributes (make sure no empty cells)
            if (!validInput())
                return;

            //TODO show progress Dialog here


            // now handle the positive button click from the
            // material design date picker

            mTrip.type = (String) mTripTypeSpinner.getSelectedItem();
            mTrip.repeating = (String) mRepeatingSpinner.getSelectedItem();
            mTrip.status = UpcomingFragment.TRIP_STATUS.UPCOMING.name();

            Timber.i("selected Date: %s", mTrip.dateInMilliSeconds);
            Timber.i("selected Time: %s", mTrip.timeInMilliSeconds);


            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mTrip.timeInMilliSeconds + mTrip.dateInMilliSeconds);
            Timber.i("New Date: %s", formatter.format(calendar.getTimeInMillis()));

            if (!validInputTime())
                return;

            Timber.i("Come here :it is valid input Time");


            //insert trip to specific user
            if (saveMode == 0)
                mTrip.pushId = userRef.push().getKey();
            else
                mTrip.setUpdated(true);

            Timber.i("id : %s", mTrip.pushId);

            if (mTrip.pushId != null) {
                userRef.child(mTrip.pushId).setValue(mTrip);
                String message = (saveMode != 0) ? (saveMode == 1) ? getString(R.string.trip_edit_success)
                        : getString(R.string.trip_updated_success)
                        : getString(R.string.Trip_added_successfully);

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                Timber.i("initAddTrip: add trip successfully");
                Navigation.findNavController(binding.getRoot()).navigate(AddTripFragmentDirections.actionAddTripFragmentToUpcomingFragment());
            }
        });
    }

    private boolean validInputTime() {
        String repeating = (String) mRepeatingSpinner.getSelectedItem();
        if (repeating.equals(repeatingOptions.get(0))) {
            if (mTrip.dateInMilliSeconds < getMillisecondsFromString(DATE_FORMAT.format(Calendar.getInstance().getTime()), DATE_FORMAT)) {
                Toast.makeText(context, "Change Date Calender, you can not add past date!!", Toast.LENGTH_SHORT).show();
                return false;
            } else if (mTrip.dateInMilliSeconds >= getMillisecondsFromString(DATE_FORMAT.format(Calendar.getInstance().getTime()), DATE_FORMAT) &&
                    mTrip.timeInMilliSeconds - 60000 < getMillisecondsFromString(TIME_FORMAT.format(Calendar.getInstance().getTime()), TIME_FORMAT)) {
                Toast.makeText(context, "Change Day Time, you can not add past time!!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            mTrip.timeInMilliSeconds += UpcomingFragment.getRepeatInterval(repeating);
        }
        return true;
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
                performAutoCompletePlaceAction();
            }
        });
        mEndPointEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                startPointClicked = false;
                performAutoCompletePlaceAction();
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
                mTrip.name = s.toString();
            }
        });
    }

    private void initComponents() {
        mTripName = binding.tripNameInput;
        mPickDateEditText = binding.calenderEditText;
        mPickTimeEditText = binding.timeEditText;
        mSaveTripButton = binding.addTripBtn;
        mStartPointEditText = binding.startPointSearchView;
        mEndPointEditText = binding.endPointSearchView;
        mRepeatingSpinner = binding.repeatingSpinner;
        mTripTypeSpinner = binding.tripType;
        myLayout = binding.myLayout;
        context = getActivity();
        setComponentsAction();
    }

    @SuppressLint("SimpleDateFormat")
    private void initTimePicker() {
        mPickTimeEditText.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            MaterialTimePicker picker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(now.get(Calendar.HOUR_OF_DAY))
                            .setMinute(now.get(Calendar.MINUTE))
                            .setTitleText("Select time")
                            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                            .build();

            picker.addOnPositiveButtonClickListener(v1 -> {
                Date date = null;
                try {
                    date = new SimpleDateFormat("HH:mm").parse(picker.getHour() + ":" + picker.getMinute());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null) {
                    mPickTimeEditText.setText(new SimpleDateFormat("hh:mm aa").format(date));
                    mTrip.timeInMilliSeconds = date.getTime();
                    Timber.i("Time Picker : %s", mTrip.timeInMilliSeconds);
                    Timber.i("Time Picker : %s", TIME_FORMAT.format(mTrip.timeInMilliSeconds));
                }
            });

            picker.show(getChildFragmentManager(), "MaterialTimePicker");
        });
    }

    private void initDatePicker() {

        //range of date
        CalendarConstraints.DateValidator dateValidator = DateValidatorPointForward.from((
                getMillisecondsFromString(DATE_FORMAT.format(Calendar.getInstance().getTime()), DATE_FORMAT)
        ));
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT TRIP DATE")
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(dateValidator).build())
                .build();
        mPickDateEditText.setOnClickListener(v -> materialDatePicker.show(getParentFragmentManager(), DATE_PICKER_TAG));
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    if (materialDatePicker.getSelection() != null) {
                        mTrip.dateInMilliSeconds = (long) materialDatePicker.getSelection();
                        Timber.i("Date Picker : %s", mTrip.dateInMilliSeconds);
                        mPickDateEditText.setText(materialDatePicker.getHeaderText());
                    }
                });

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


    private boolean validInput() {
        if (mTrip.name == null || mTrip.name.isEmpty())
            Toast.makeText(context, "Trip Name Is Empty!!", Toast.LENGTH_SHORT).show();
        else if (mTrip.locationFrom == null)
            Toast.makeText(context, "Start Point Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mTrip.locationTo == null)
            Toast.makeText(context, "end Point Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mPickTimeEditText.getText().toString().isEmpty())
            Toast.makeText(context, "Time Not Specified!!", Toast.LENGTH_SHORT).show();
        else if (mPickDateEditText.getText().toString().isEmpty())
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

    public static long getMillisecondsFromString(String s, SimpleDateFormat format) {
        try {
            return Objects.requireNonNull(format.parse(s)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

}