package com.arny.mvpclean.data.repository.main

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.utils.SingletonHolder

@Database(entities = [(CleanFolder::class)], version = 1, exportSchema = false)
abstract class MainDB : RoomDatabase() {

    companion object : SingletonHolder<MainDB, Context>({
        Room.databaseBuilder(it.applicationContext, MainDB::class.java, Consts.DB.DB_NAME)
                .build()
    })
    abstract val folderDao: CleanFolderDao

}
