package com.arny.mvpclean.data.repository.main

import android.content.Context
import com.arny.mvpclean.CleanApp
import com.arny.mvpclean.data.source.base.BaseRepository

class MainRepositoryImpl : BaseRepository, CleanRepository {
    private object Holder {
        val INSTANCE = MainRepositoryImpl()
    }

    companion object {
        val instance: MainRepositoryImpl by lazy { Holder.INSTANCE }
    }

    override fun getContext(): Context {
        return CleanApp.appContext
    }

    override fun getFolderDao(): CleanFolderDao {
        return MainDB.getInstance(getContext()).folderDao
    }

}

