package com.example.easytripplanner.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.easytripplanner.Fragments.UpcomingFragment;
import com.example.easytripplanner.activities.MyDialog;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.utility.Parcelables;

import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, MyDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Timber.i("Trip : %s", Parcelables.toParcelable(intent.getByteArrayExtra(UpcomingFragment.TRIP),
                Trip.CREATOR));
        context.startActivity(intent);
    }
}