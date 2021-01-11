package com.example.easytripplanner;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.easytripplanner.Fragments.upcoming.TripsViewFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private final String[] tabTitles;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        tabTitles = new String[]{context.getResources().getString(R.string.upcoming_trips), context.getResources().getString(R.string.past_trips)};
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return (position == 0) ? TripsViewFragment.newInstance(0) : TripsViewFragment.newInstance(1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}