package com.example.easytripplanner.activities;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.easytripplanner.Fragments.TripsViewFragment;
import com.example.easytripplanner.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_HASH_CODE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_ID;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_LOCATION_NAME;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_LOC_LATITUDE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_LOC_LONGITUDE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_NAME;
import static com.example.easytripplanner.activities.MainActivity.PRIMARY_CHANNEL_ID;

public class MyDialog extends AppCompatActivity {

    private String tripName;
    private String tripLocAddress;
    private double tripLocLat;
    private double tripLocLong;
    private int tripHashCode;
    private String tripID;
    private Intent receiverIntent;
    private NotificationManager mNotificationManager;
    private boolean isNotificationFired;

    private static final String TAG = "MyDialog";
    private static String GROUP_KEY = "com.android.example.EasyTripPlanner";
    public static String NOTIFICATION_STATUS = "Notification Status";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        getIntentData();
        displayAlert();
    }

    private void getIntentData() {

        mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        this.receiverIntent = getIntent();
        tripName = getIntent().getStringExtra(TRIP_NAME);
        tripLocAddress = getIntent().getStringExtra(TRIP_LOCATION_NAME);

        Log.i(TAG, "getIntentData: tripName: " + tripName);
        Log.i(TAG, "getIntentData: tripLocAddress: " + tripLocAddress);


        tripLocLat = getIntent().getDoubleExtra(TRIP_LOC_LATITUDE, 0);
        tripLocLong = getIntent().getDoubleExtra(TRIP_LOC_LONGITUDE, 0);

        tripHashCode = getIntent().getIntExtra(TRIP_HASH_CODE, 0);
        tripID = getIntent().getStringExtra(TRIP_ID);
    }

    private void displayAlert() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                .setTitle("Reminder: " + tripName)
                .setMessage(("to : " + tripLocAddress))
                .setPositiveButton("START", (dialog, which) -> {
                    changeTripStatus(TripsViewFragment.TRIP_STATUS.DONE.name());
                    startNavigation();
                    mNotificationManager.cancel(tripHashCode);
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    changeTripStatus(TripsViewFragment.TRIP_STATUS.CANCELED.name());
                    mNotificationManager.cancel(tripHashCode);
                })
                .setNeutralButton("SNOOZE", (dialog, which) -> {
                    isNotificationFired = receiverIntent.getBooleanExtra(NOTIFICATION_STATUS, false);
                    if (!isNotificationFired) {
                        receiverIntent.putExtra(NOTIFICATION_STATUS, true);
                        deliverNotification();
                    }

                })
                .setOnDismissListener(dialog -> {
                    finishAndRemoveTask();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().addFlags(FLAG_TURN_SCREEN_ON);
        alertDialog.show();
    }


    private void changeTripStatus(String value) {
        //edit trip attribute status to Canceled
        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference currentUserRef = null;
        if (userId != null) {
            currentUserRef = database.getReference("Users").child(userId);
        }
        if (currentUserRef != null)
            currentUserRef.child(tripID).child("status").setValue(value);


        //remove from sharedPreference
        SharedPreferences sharedPref = Objects.requireNonNull(getSharedPreferences("Save", MODE_PRIVATE));
        if (sharedPref.contains(tripID)) {
            //delete it from sharedPreference
            sharedPref.edit().remove(tripID).apply();
        }
    }


    /**
     * Builds and delivers the notification.
     */
    private void deliverNotification() {
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, tripHashCode, receiverIntent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stand_up)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText(tripName + " !!!")
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setGroup(GROUP_KEY);

        // Deliver the notification
        mNotificationManager.notify(tripHashCode, builder.build());
    }


    @SuppressLint("QueryPermissionsNeeded")
    public void startNavigation() {
        Uri uri = Uri.parse("google.navigation:q=" + tripLocLat + "," + tripLocLong);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (receiverIntent.resolveActivity(this.getPackageManager()) != null) {
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(mapIntent);
        } else {
            Toast.makeText(this, "please install google maps!!!", Toast.LENGTH_SHORT).show();
        }
    }
}