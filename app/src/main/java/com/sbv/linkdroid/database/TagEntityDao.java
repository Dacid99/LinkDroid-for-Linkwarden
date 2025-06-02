package com.sbv.linkdroid.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TagEntityDao {
    @Query("SELECT * FROM TagEntity")
    List<TagEntity> getAll();

    @Insert
    void insertAll(List<TagEntity> entities);

    @Delete
    void delete(TagEntity entry);


    default void deleteAll() {
        getAll().forEach(this::delete);
    }

    default void refresh(List<TagEntity> entities) {
        getAll().forEach(this::delete);
        insertAll(entities);
    }
}