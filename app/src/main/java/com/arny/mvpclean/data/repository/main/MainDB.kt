package com.arny.mvpclean.data.repository.main

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.arny.mvpclean.data.models.CleanFolder

@Database(entities = [(CleanFolder::class)], version = 1, exportSchema = false)
abstract class MainDB : RoomDatabase() {
    abstract val folderDao: CleanFolderDao

}
