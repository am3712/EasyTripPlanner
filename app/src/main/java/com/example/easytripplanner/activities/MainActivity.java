package com.example.easytripplanner.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.FragmentLoginBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button button;
    private FragmentLoginBinding binding;

    private static final String TAG = "MainActivity";

    // Notification channel ID.
    public static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize NavController.
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        //Initialize Bottom Navigation View.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        //Pass the ID's of Different destinations
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.upcomingFragment, R.id.historyFragment, R.id.mappedFragment).build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(
                toolbar, navController, appBarConfiguration);


        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment) {
                toolbar.setVisibility(View.GONE);
                bottomNav.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.registerFragment || destination.getId() == R.id.addTripFragment) {
                toolbar.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.VISIBLE);
            }
        });
        toolbar.setOnMenuItemClickListener(item -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Logout " + item.getTitle(),
                    Toast.LENGTH_SHORT).show();
            if (Objects.requireNonNull(navController.getCurrentDestination()).getId() != R.id.upcomingFragment)
                navController.popBackStack();
            navController.popBackStack();
            navController.navigate(R.id.loginFragment);
            return true;
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.logout_menu, menu);
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

}