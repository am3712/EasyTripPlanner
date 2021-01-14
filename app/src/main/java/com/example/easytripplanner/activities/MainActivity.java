package com.example.easytripplanner.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.easytripplanner.Fragments.PastTripFragment;
import com.example.easytripplanner.Fragments.TripsViewFragment;
import com.example.easytripplanner.MapFragment;
import com.example.easytripplanner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.shrikanthravi.customnavigationdrawer2.data.MenuItem;
import com.shrikanthravi.customnavigationdrawer2.widget.SNavigationDrawer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    SNavigationDrawer sNavigationDrawer;
    Class aClass;

    // configure icons
    private final int[] imageResId = {
            R.drawable.baseline_event_note_24,
            R.drawable.baseline_event_available_24};
    private static final String TAG = "MainActivity";

    // Notification channel ID.
    public static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sNavigationDrawer = findViewById(R.id.navigation_drawer);
        List<MenuItem> itemList = new ArrayList<>();
        //add menu item in list
        itemList.add(new MenuItem("Upcoming", R.drawable.car));
        itemList.add(new MenuItem("History", R.drawable.history));
        itemList.add(new MenuItem("Maps", R.drawable.download));
        itemList.add(new MenuItem("Logout", R.drawable.log));
        // itemList.add(new MenuItem("About As",R.drawable.ic_baseline_info_24));
        // itemList.add(new MenuItem("Logout",R.drawable.ic_baseline_power_settings_new_24));

        //set menu item
        sNavigationDrawer.setMenuItemList(itemList);
        //set defualt title
        sNavigationDrawer.setAppbarTitleTV("Upcoming");
        //defualt fragment
        aClass = TripsViewFragment.class;
        //open fragment
        openFragment();
        sNavigationDrawer.setOnMenuItemClickListener(new SNavigationDrawer.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClicked(int position) {
                switch (position) {
                    case 0:
                        aClass = TripsViewFragment.class;
                        break;
                    case 1:
                        aClass = PastTripFragment.class;
                        break;
                    case 2:
                        aClass = MapFragment.class;
                        break;
                    case 3:
                        FirebaseAuth.getInstance().signOut();

                        //aClass=LoginActivity.class;

                        // Log.i(TAG, "onMenuItemClicked: current Usr: " + FirebaseAuth.getInstance().getCurrentUser());
                        MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        break;
                }

            }
        });

        sNavigationDrawer.setDrawerListener(new SNavigationDrawer.DrawerListener() {
            @Override
            public void onDrawerOpening() {

            }

            @Override
            public void onDrawerClosing() {
                openFragment();
            }

            @Override
            public void onDrawerOpened() {

            }

            @Override
            public void onDrawerClosed() {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        findViewById(R.id.add_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewTripActivity.class);
            startActivity(intent);
        });

        // Create the notification channel.
        createNotificationChannel();
    }


    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {

        // Create a notification manager object.
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            "Easy trip planner notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void openFragment() {
        try {
            Fragment fragment = (Fragment) aClass.newInstance();
            //open fragment
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.frame_layout, fragment)
                    .commit();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }


}