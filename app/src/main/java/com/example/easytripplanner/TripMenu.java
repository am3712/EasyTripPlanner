package com.example.easytripplanner;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easytripplanner.Fragments.TripsViewFragment;

public class TripMenu extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    Button btnMore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_item_view);
        btnMore=findViewById(R.id.btnMore);

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu =new PopupMenu(TripMenu.this,v);
                popupMenu.getMenuInflater().inflate(R.menu.trip_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return false;
                    }
                });
                //popupMenu.inflate(R.menu.trip_menu);
                popupMenu.show();
            }
        });


    }



    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return true;
    }
}
