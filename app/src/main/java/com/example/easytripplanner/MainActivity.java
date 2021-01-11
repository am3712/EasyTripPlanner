package com.example.easytripplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // configure icons
    private final int[] imageResId = {
            R.drawable.baseline_event_note_black_18,
            R.drawable.baseline_event_available_black_18};
    private static final String TAG = "MainActivity";
    public static String POSITION = "POSITION";


    TabLayout tabLayout;
    ViewPager pager;
    SectionsPagerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                MainActivity.this);


        //tabs
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        pager = findViewById(R.id.view_pager);
        pager.setOffscreenPageLimit(2);

        pager.setAdapter(mAdapter);

        pager.setPageTransformer(true, new RotateUpTransformer());
        //pager.setPageTransformer(true, new AccordionTransformer());
        //pager.setPageTransformer(true, new BackgroundToForegroundTransformer());
        //pager.setPageTransformer(true, new ZoomInTransformer());
        //pager.setPageTransformer(true, new CubeInTransformer());
        //pager.setPageTransformer(true, new DepthPageTransformer());
        //pager.setPageTransformer(true, new TabletTransformer());
        //pager.setPageTransformer(true, new FlipHorizontalTransformer());

        // Give the TabLayout the ViewPager
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);

        findViewById(R.id.add_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewTripActivity.class);
            startActivity(intent);
        });


        for (int i = 0; i < imageResId.length; i++) {
            Objects.requireNonNull(tabLayout.getTabAt(i)).setIcon(imageResId[i]);
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, tabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        pager.setCurrentItem(savedInstanceState.getInt(POSITION));
    }

}