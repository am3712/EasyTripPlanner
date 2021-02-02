package com.myfirstgoogleapp.easytripplanner.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.myfirstgoogleapp.easytripplanner.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    // Notification channel ID.
    public static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    BottomNavigationView bottomNav;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize NavController.
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        //Initialize Bottom Navigation View.
        bottomNav = findViewById(R.id.bottom_nav);

        //Pass the ID's of Different destinations
        appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.upcomingFragment, R.id.historyFragment, R.id.mappedFragment, R.id.aboutUs).build();
        toolbar = findViewById(R.id.toolbar);


        NavigationUI.setupWithNavController(bottomNav, navController);


        // Create the notification channel.
        createNotificationChannel();

        setSupportActionBar(toolbar);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


    }

    @Override
    protected void onResume() {
        super.onResume();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment) {
                toolbar.setVisibility(View.GONE);
                bottomNav.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.addTripFragment || destination.getId() == R.id.addNotesFragment) {
                toolbar.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.registerFragment) {
                toolbar.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.GONE);
                if (toolbar.getMenu().findItem(R.id.logout_menu_item) != null)
                    toolbar.getMenu().findItem(R.id.logout_menu_item).setVisible(false);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.VISIBLE);
                if (toolbar.getMenu().findItem(R.id.logout_menu_item) != null)
                    toolbar.getMenu().findItem(R.id.logout_menu_item).setVisible(true);
            }
        });
        toolbar.setOnMenuItemClickListener(item -> {
            FirebaseAuth.getInstance().signOut();
            if (Objects.requireNonNull(navController.getCurrentDestination()).getId() != R.id.upcomingFragment)
                navController.popBackStack();
            navController.popBackStack();
            navController.navigate(R.id.loginFragment);
            return true;
        });
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

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }
}