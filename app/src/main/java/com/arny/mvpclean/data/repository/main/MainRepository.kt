package com.arny.mvpclean.data.repository.main

import android.content.Context
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.repository.BaseRepository
import com.arny.mvpclean.CleanApp
import com.arny.mvpclean.CleanApp.Companion.applicationComponent
import com.arny.mvpclean.data.models.CleanFolder
import io.reactivex.Observable
import java.io.File

class MainRepository : BaseRepository() {
    init {
        CleanApp.applicationComponent.inject(this)
    }

    override fun getContext(): Context {
        return applicationComponent.getContext()
    }

    fun getList(): Observable<MutableList<CleanFolder>>? {
        return Observable.fromCallable { applicationComponent.getDb().folderDao.cleanFileList }
                .map { list ->
                    for (cleanFolder in list) {
                        cleanFolder.title = FileUtils.getFilenameWithExtention(cleanFolder.path)
                        cleanFolder.size = FileUtils.getFolderSize(File(cleanFolder.path))
                    }
                    list
                }
    }

    fun addFolderToClean(path: String): Boolean {
        return applicationComponent.getDb().folderDao.insert(CleanFolder(path)) > 0
    }

    fun removeFolder(id: Long): Boolean {
        return applicationComponent.getDb().folderDao.delete(id) > 0
    }
}

