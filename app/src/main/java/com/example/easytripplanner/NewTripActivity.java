package com.example.easytripplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

public class NewTripActivity extends AppCompatActivity {

    ImageButton mPickDateButton;
    ImageButton timeButton;
    Button addTripButton;
    EditText tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        init();

    }

    private void init() {
        mPickDateButton = findViewById(R.id.calender_btn);
        timeButton = findViewById(R.id.timeBtn);
        addTripButton = findViewById(R.id.add_trip_btn);


        initDateButton();
    }

    private void initDateButton() {
        // now create instance of the material date picker 
        // builder make sure to add the "datePicker" which 
        // is normal material date picker which is the first 
        // type of the date picker in material design date 
        // picker 
        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker();

        // now define the properties of the 
        // materialDateBuilder that is title text as SELECT A DATE 
        materialDateBuilder.setTitleText("SELECT A DATE");

        // now create the instance of the material date 
        // picker 
        final MaterialDatePicker materialDatePicker = materialDateBuilder.build();

        // handle select date button which opens the 
        // material design date picker 
        mPickDateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // getSupportFragmentManager() to 
                        // interact with the fragments 
                        // associated with the material design 
                        // date picker tag is to get any error 
                        // in logcat 
                        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
                    }
                });

        // now handle the positive button click from the 
        // material design date picker 
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {

                    // if the user clicks on the positive 
                    // button that is ok button update the 
                    // selected date 
                    //mShowSelectedDateText.setText("Selected Date is : " + materialDatePicker.getHeaderText());
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                    // in the above statement, getHeaderText 
                    // is the selected date preview from the 
                    // dialog 
                });
    }
}