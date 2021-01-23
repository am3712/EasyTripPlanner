package com.example.easytripplanner.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.example.easytripplanner.Fragments.UpcomingFragment;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.utility.Parcelables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.ALARM_SERVICE;
import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP;
import static com.example.easytripplanner.Fragments.UpcomingFragment.getRepeatInterval;

public class RestartAlarm extends BroadcastReceiver {
    private Context context;
    ValueEventListener listener;

    public RestartAlarm() {

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            this.context = context;
            init();
        }
    }

    private void init() {


        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null)
            return;


        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference currentUserRef = database.getReference("Users").child(userId);
        currentUserRef.keepSynced(true);

        currentUserRef
                .orderByChild("status")
                .equalTo(UpcomingFragment.TRIP_STATUS.UPCOMING.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            if (trip != null && trip.name != null) {
                                setAlarm(trip);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void setAlarm(Trip t) {
        //set Alarm
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        final Intent intent = new Intent(context, AlarmReceiver.class);

        intent.putExtra(TRIP, Parcelables.toByteArray(t));

        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(context, t.pushId.hashCode(),
                intent, PendingIntent.FLAG_NO_CREATE);

        if (t.repeating.equals("No Repeated")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmMgr.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        t.dateInMilliSeconds + t.timeInMilliSeconds,
                        notifyPendingIntent);
            } else {
                alarmMgr.setExact(
                        AlarmManager.RTC_WAKEUP,
                        t.dateInMilliSeconds + t.timeInMilliSeconds,
                        notifyPendingIntent);
            }
        } else {

            alarmMgr.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    t.dateInMilliSeconds + t.timeInMilliSeconds,
                    getRepeatInterval(t.repeating),
                    notifyPendingIntent);
        }
    }
}
