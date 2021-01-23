package com.example.easytripplanner.activities;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.easytripplanner.Fragments.UpcomingFragment;
import com.example.easytripplanner.R;
import com.example.easytripplanner.models.Trip;
import com.example.easytripplanner.services.FloatingViewService;
import com.example.easytripplanner.utility.Parcelables;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

import static com.example.easytripplanner.Fragments.UpcomingFragment.TRIP_ID;
import static com.example.easytripplanner.Fragments.UpcomingFragment.getRepeatInterval;
import static com.example.easytripplanner.activities.MainActivity.PRIMARY_CHANNEL_ID;

public class MyDialog extends AppCompatActivity {

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;
    public static String NOTIFICATION_STATUS = "Notification Status";
    private static final String GROUP_KEY = "com.android.example.EasyTripPlanner";
    private Trip trip;
    private Intent receiverIntent;
    private NotificationManager mNotificationManager;
    private boolean isNotificationFired;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        turnScreenOn();
        setTitle("");
        displayAlert();
    }

    private void getIntentData() {

        mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        receiverIntent = getIntent();
        trip = Parcelables.toParcelable(receiverIntent.getByteArrayExtra(UpcomingFragment.TRIP),
                Trip.CREATOR);
        Timber.i("Trip : %s", trip);
    }

    private void displayAlert() {
        isNotificationFired = receiverIntent.getBooleanExtra(NOTIFICATION_STATUS, false);
        if (!isNotificationFired) {
            mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
            mediaPlayer.start();
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                .setTitle("Reminder: " + trip.name)
                .setMessage(("to : " + trip.locationTo.Address))
                .setPositiveButton("START", (dialog, which) -> checkOverlayPermissionAndStartNav())
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    changeTripStatus(UpcomingFragment.TRIP_STATUS.CANCELED.name());
                    mNotificationManager.cancel(trip.pushId.hashCode());
                    finishAndRemoveTask();
                })
                .setNeutralButton("SNOOZE", (dialog, which) -> {
                    if (!isNotificationFired) {
                        receiverIntent.putExtra(NOTIFICATION_STATUS, true);
                        deliverNotification();
                    }
                    finishAndRemoveTask();
                }).setOnDismissListener(dialog -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                });
        AlertDialog alertDialog = builder.create();

        alertDialog.setCanceledOnTouchOutside(false);

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
        if (currentUserRef != null) {
            if (trip != null && trip.pushId != null) {
                if (!trip.repeating.equalsIgnoreCase("No Repeated"))
                    currentUserRef.child(trip.pushId).child("timeInMilliSeconds").setValue(trip.timeInMilliSeconds + getRepeatInterval(trip.repeating));
                else
                    currentUserRef.child(trip.pushId).child("status").setValue(value);

                currentUserRef.child(trip.pushId).child("notes").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && value.equals(UpcomingFragment.TRIP_STATUS.DONE.name())) {
                        startFloatingService();
                        finishAndRemoveTask();
                    }
                });

            }
        }

    }

    private void startFloatingService() {
        Intent noteIntent = new Intent(MyDialog.this, FloatingViewService.class);
        Timber.i("Trip Id : %s", trip.pushId);
        noteIntent.putExtra(TRIP_ID, trip.pushId);
        MyDialog.this.startService(noteIntent);
    }


    /**
     * Builds and delivers the notification.
     */
    private void deliverNotification() {
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, trip.pushId.hashCode(), receiverIntent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stand_up)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText(trip.name + " !!!")
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setGroup(GROUP_KEY);

        // Deliver the notification
        mNotificationManager.notify(trip.pushId.hashCode(), builder.build());
    }


    @SuppressLint("QueryPermissionsNeeded")
    public void startNavigation() {
        Uri uri = Uri.parse("google.navigation:q=" + trip.locationTo.latitude + "," + trip.locationTo.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(this.getPackageManager()) != null) {
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(mapIntent);
        } else {
            Toast.makeText(this, "please install google maps!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkOverlayPermissionAndStartNav() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                return;
            }
        }
        startAction();
    }

    private void startAction() {
        startNavigation();
        mNotificationManager.cancel(trip.pushId.hashCode());
        changeTripStatus(UpcomingFragment.TRIP_STATUS.DONE.name());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            startAction();
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            } else {
                startFloatingService();
                finishAndRemoveTask();
            }
        }
    }

    private void turnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            final Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }


}