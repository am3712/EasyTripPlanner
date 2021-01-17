package com.example.easytripplanner.firebase;

import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

public class MyFirebaseApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Timber.plant(new Timber.DebugTree());
    }
}