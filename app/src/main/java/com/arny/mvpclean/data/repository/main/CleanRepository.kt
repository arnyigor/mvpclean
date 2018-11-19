package com.arny.mvpclean.data.repository.main

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.utils.*
import io.reactivex.Observable
import java.io.File

interface CleanRepository {
    fun getFolderDao(): CleanFolderDao
    fun getContext(): Context
    fun clearFolders(stopwatch: Stopwatch): Observable<Boolean>? {
        return getList().map { folderFiles ->
            println("Clean folders time " + DateTimeUtils.getDateTime())
            for (folderFile in folderFiles) {
                val rem = FileUtils.cleanDirectory(folderFile.path)
                if (!rem) {
                    stopwatch.stop()
                    return@map false
                } else {
                    ToastMaker.toastSuccess(getContext(), "Файлы удалены")
                    val updateIntent = Intent(UpdateManager.INTENT_UPDATE_MANAGER_STATE)
                    updateIntent.addCategory(Intent.CATEGORY_DEFAULT)
                    updateIntent.putExtra(UpdateManager.INTENT_UPDATE_MANAGER_STATE_UPDATED, true)
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(updateIntent)
                }
            }
            stopwatch.stop()
            true
        }
    }

    fun getList(): Observable<ArrayList<CleanFolder>> {
        return Observable.fromCallable { getFolderDao().cleanFileList }
                .map { list ->
                    for (cleanFolder in list) {
                        cleanFolder.title = FileUtils.getFilenameWithExtention(cleanFolder.path)
                        cleanFolder.size = FileUtils.getFolderSize(File(cleanFolder.path))
                    }
                    list as ArrayList
                }
    }

    fun addFolderToClean(path: String): Boolean {
        return getFolderDao().insert(CleanFolder(path)) > 0
    }

    fun removeFolder(id: Long): Boolean {
        return getFolderDao().delete(id) > 0
    }
}