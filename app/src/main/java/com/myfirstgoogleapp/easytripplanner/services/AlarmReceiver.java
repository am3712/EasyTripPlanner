package com.myfirstgoogleapp.easytripplanner.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.myfirstgoogleapp.easytripplanner.Fragments.UpcomingFragment;
import com.myfirstgoogleapp.easytripplanner.activities.MyDialog;
import com.myfirstgoogleapp.easytripplanner.models.Trip;
import com.myfirstgoogleapp.easytripplanner.utility.Parcelables;

import timber.log.Timber;

import static com.myfirstgoogleapp.easytripplanner.Fragments.UpcomingFragment.ALARM_ACTION;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i("called");
        Timber.i("intent Action : %s", intent.getAction());
        if (intent.getAction() != null && intent.getAction().equals(ALARM_ACTION)) {
            intent.setClass(context, MyDialog.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Timber.i("Trip : %s", Parcelables.toParcelable(intent.getByteArrayExtra(UpcomingFragment.TRIP),
                    Trip.CREATOR));
            context.startActivity(intent);
        }
    }

}