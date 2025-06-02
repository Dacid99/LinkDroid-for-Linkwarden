package com.sbv.linkdroid.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CollectionEntityDao {

    @Query("SELECT * FROM CollectionEntity")
    List<CollectionEntity> getAll();

    @Insert
    void insertAll(List<CollectionEntity> entities);

    @Delete
    void delete(CollectionEntity entry);


    default void deleteAll() {
        getAll().forEach(this::delete);
    }

    default void refresh(List<CollectionEntity> entities) {
        getAll().forEach(this::delete);
        insertAll(entities);
    }
}
