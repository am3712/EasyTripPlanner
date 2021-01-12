package com.example.easytripplanner.broadcastreceiver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.easytripplanner.Fragments.TripsViewFragment;
import com.example.easytripplanner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_HASH_CODE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_ID;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_LOCATION_NAME;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_LOC_LATITUDE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_LOC_LONGITUDE;
import static com.example.easytripplanner.Fragments.TripsViewFragment.TRIP_NAME;
import static com.example.easytripplanner.MainActivity.PRIMARY_CHANNEL_ID;

public class AlarmReceiver extends BroadcastReceiver {

    private String tripName;
    private String tripLocAddress;
    private double tripLocLat;
    private double tripLocLong;
    private int tripHashCode;
    private String tripID;
    private Context context;
    private Intent intent;
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        tripName = intent.getStringExtra(TRIP_NAME);
        tripLocAddress = intent.getStringExtra(TRIP_LOCATION_NAME);

        tripLocLat = intent.getDoubleExtra(TRIP_LOC_LATITUDE, 0);
        tripLocLong = intent.getDoubleExtra(TRIP_LOC_LONGITUDE, 0);


        Log.i(TAG, "onReceive: TRIP_LOC_LATITUDE: " + tripLocLat);
        Log.i(TAG, "onReceive: TRIP_LOC_LONGITUDE: " + tripLocLong);


        tripHashCode = intent.getIntExtra(TRIP_HASH_CODE, 0);
        tripID = intent.getStringExtra(TRIP_ID);
        showAlertDialog();


    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Voice);
        builder
                .setTitle(tripName)
                .setMessage(("to : " + tripLocAddress))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    changeTripStatus(TripsViewFragment.TRIP_STATUS.CANCELED.name());
                    dialog.dismiss();
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    changeTripStatus(TripsViewFragment.TRIP_STATUS.DONE.name());
                    startNavigation();
                    dialog.dismiss();
                }).setNeutralButton("Snooze", (dialog, which) -> {
            deliverNotification();
        });


        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            int type;
            type = WindowManager.LayoutParams.TYPE_TOAST;
            alertDialog.getWindow().setType(type);
        }
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.black));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.black));
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(context.getResources().getColor(R.color.black));
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
        SharedPreferences sharedPref = Objects.requireNonNull(context.getSharedPreferences("Save", MODE_PRIVATE));
        if (sharedPref.contains(tripID)) {
            //delete it from sharedPreference
            sharedPref.edit().remove(tripID).apply();
        }
    }


    /**
     * Builds and delivers the notification.
     */
    private void deliverNotification() {

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the content intent for the notification, which launches
        // this activity

        PendingIntent contentPendingIntent = PendingIntent.getBroadcast
                (context, tripHashCode, intent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stand_up)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(tripName)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true);

        // Deliver the notification
        mNotificationManager.notify(tripHashCode, builder.build());
    }


    @SuppressLint("QueryPermissionsNeeded")
    public void startNavigation() {
        Uri uri = Uri.parse("google.navigation:q=" + tripLocLat + "," + tripLocLong);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mapIntent);
        } else {
            Toast.makeText(context, "please install google maps!!!", Toast.LENGTH_SHORT).show();
        }


    }

}