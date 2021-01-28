package com.example.easytripplanner.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.easytripplanner.R;

public class AboutFragment extends Fragment {

TextView textAbout;
TextView textNames;
    public AboutFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_about, container, false);
        textAbout=view.findViewById(R.id.textAbout);
        textNames=view.findViewById(R.id.textNames);

        textAbout.setText("Android Mobile Application that helps the user to record his planned trips with the start and end Geo-points, accompanied with the date and time of the trip and notes. The application should remind the user with his trips on the time specified by the user. In addition, the application should navigate the user to his destination. After that, the application should keep track with the upcoming and past trips.");
        textNames.setText("1-Abdulrahman Mustafa Salah\n" +
                "\n" +
                "2-Aya Mohamed Mohamed Abo Eid\n" +
                "\n" +
                "3-Mariam Moamen Abdelsalam Abdellatif");
        return view;
    }
}