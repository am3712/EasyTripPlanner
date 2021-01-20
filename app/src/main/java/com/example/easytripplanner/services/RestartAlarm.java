package com.example.easytripplanner.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.example.easytripplanner.Fragments.UpcomingFragment;
import com.example.easytripplanner.models.Trip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_HASH_CODE;
import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_ID;
import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_LOCATION_NAME;
import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_LOC_LATITUDE;
import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_NAME;

public class RestartAlarm extends BroadcastReceiver {
    private Context context;
    private ArrayList<Trip> trips;
    DatabaseReference currentUserRef;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            this.context=context;
            MediaPlayer mediaPlayer =MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
            mediaPlayer.start();
        }
    }


    private void setAlarm(){
        SharedPreferences sharedPref = context.getSharedPreferences("Save", MODE_PRIVATE);

        //set Alarm
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        for (Trip t : trips) {
            //first scenario if trip is forgotten then do not fire and if found in sharedPreferences delete it
            if (t.status.equalsIgnoreCase(UpcomingFragment.TRIP_STATUS.FORGOTTEN.name())) {
                if (sharedPref.contains(t.pushId)) {
                    //delete it from sharedPreference
                    sharedPref.edit().remove(t.pushId).apply();
                }
            } else {
                final Intent intent = new Intent(context, AlarmReceiver.class);

                intent.putExtra(TRIP_NAME, t.name);
                intent.putExtra(TRIP_ID, t.pushId);
                intent.putExtra(TRIP_HASH_CODE, t.pushId.hashCode());
                intent.putExtra(TRIP_LOCATION_NAME, t.locationTo.Address);
                intent.putExtra(UpcomingFragment.TRIP_LOC_LONGITUDE, t.locationTo.longitude);
                intent.putExtra(TRIP_LOC_LATITUDE, t.locationTo.latitude);


                PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(context, t.pushId.hashCode(),
                        intent, PendingIntent.FLAG_NO_CREATE);
                if (notifyPendingIntent == null)
                    notifyPendingIntent = PendingIntent.getBroadcast
                            (context, t.pushId.hashCode(), intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                if (t.repeating.equals("No Repeated")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.timeInMilliSeconds, notifyPendingIntent);
                    } else {
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, t.timeInMilliSeconds, notifyPendingIntent);
                    }
                } else {
                    long repeatInterval;
                    long ONE_DAY = 86400000;
                    switch (t.repeating) {
                        case "Repeated Daily":
                            repeatInterval = ONE_DAY;
                            break;
                        case "Repeated weekly":
                            repeatInterval = ONE_DAY * 7;
                            break;
                        case "Repeated Monthly":
                            repeatInterval = ONE_DAY * 7 * 4;
                            break;
                        default:
                            repeatInterval = 0;
                    }

                    alarmMgr.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            t.timeInMilliSeconds,
                            repeatInterval,
                            notifyPendingIntent);
                }
            }
        }

    }
    DatabaseReference finalCurrentUserRef = currentUserRef;
   ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Calendar calendar = Calendar.getInstance();
            int count = 0;
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip != null) {
                    calendar.setTimeInMillis(trip.timeInMilliSeconds);
                    trip.setDate(UpcomingFragment.formatter.format(calendar.getTime()));

                    if ( trip.timeInMilliSeconds < System.currentTimeMillis()) {
                        finalCurrentUserRef.child(trip.pushId).child("status").setValue("FORGOTTEN");
                    }
                    trips.add(trip);

                    count++;
                    if (count >= snapshot.getChildrenCount() ) {
                        Collections.sort(trips);

                       setAlarm();
                    }
                }
            }

        }

       @Override
       public void onCancelled(@NonNull DatabaseError error) {

       }


   };
}