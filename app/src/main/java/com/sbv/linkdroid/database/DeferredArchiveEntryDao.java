package com.sbv.linkdroid.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeferredArchiveEntryDao {
    @Query("SELECT * FROM DeferredArchiveEntry")
    List<DeferredArchiveEntry> getAll();

    @Query("SELECT * FROM DeferredArchiveEntry WHERE uid IN (:ids)")
    List<DeferredArchiveEntry> loadAllByIds(int[] ids);

    @Query("SELECT * FROM DeferredArchiveEntry WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    DeferredArchiveEntry findByName(String first, String last);

    @Insert
    void insertAll(DeferredArchiveEntry... entries);

    @Delete
    void delete(DeferredArchiveEntry entry);
}