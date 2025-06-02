package com.sbv.linkdroid.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        DeferredArchiveEntry.class,
        TagEntity.class,
        CollectionEntity.class
    },
    version = 1,
    exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract DeferredArchiveEntryDao linkDao();
    public abstract TagEntityDao tagDao();
    public abstract CollectionEntityDao collectionsDao();

    public static AppDatabase get(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "linkdroid").build();
    }
}