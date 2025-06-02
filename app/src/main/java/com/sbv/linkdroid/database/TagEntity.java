package com.sbv.linkdroid.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TagEntity {

    @NonNull
    @PrimaryKey
    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    public String toString() {
        return this.name;
    }


}
