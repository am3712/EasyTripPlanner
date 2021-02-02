package com.myfirstgoogleapp.easytripplanner.models;

public class Note {
    public String text;
    public boolean checked;
    public String id;

    public Note() {
    }

    public Note(String text, boolean checked) {
        this.text = text;
        this.checked = checked;
    }


    public Note(String text, boolean checked, String id) {
        this.text = text;
        this.checked = checked;
        this.id = id;
    }
}
