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

public class TripMenu extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    Button btnMore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_item_view);
        btnMore=findViewById(R.id.btnMore);




    }
       public void ShowPopup (View view){
           PopupMenu popupMenu =new PopupMenu(this,view);
           popupMenu.setOnMenuItemClickListener(this);
           popupMenu.inflate(R.menu.trip_menu);
           popupMenu.show();

       }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return true;
    }
}
