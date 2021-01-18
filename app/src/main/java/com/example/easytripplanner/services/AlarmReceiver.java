package com.example.easytripplanner.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.easytripplanner.activities.MyDialog;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, MyDialog.class);
        context.startActivity(intent);
    }
}