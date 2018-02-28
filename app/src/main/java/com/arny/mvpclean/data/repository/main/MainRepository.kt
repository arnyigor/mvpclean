package com.arny.mvpclean.data.repository.main

import android.content.Context
import com.arny.arnylib.files.FileUtils
import com.arny.mvpclean.data.models.CleanFolder
import io.reactivex.Observable
import java.io.File

fun getList(context: Context): Observable<MutableList<CleanFolder>>? {
    return Observable.fromCallable { MainDB.getInstance(context).folderDao.cleanFileList }
            .map { list->
                for (cleanFolder in list) {
                    cleanFolder.title = FileUtils.getFilenameWithExtention(cleanFolder.path)
                    cleanFolder.size = FileUtils.getFolderSize(File(cleanFolder.path))
                }
                list
            }
}

fun addFolderToClean(context: Context, path: String): Boolean {
    return MainDB.getInstance(context).folderDao.insert(CleanFolder(path)) > 0
}

fun removeFolder(context: Context, id: Long): Boolean {
    return MainDB.getInstance(context).folderDao.delete(id) > 0
}
