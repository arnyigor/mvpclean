package com.arny.mvpclean.data.repository.main

import android.content.Context
import com.arny.mvpclean.data.models.CleanFolder
import io.reactivex.Observable

fun getList(context: Context): Observable<MutableList<CleanFolder>>? {
    return Observable.fromCallable { MainDB.getInstance(context).folderDao.cleanFileList }
}

fun addFolderToClean(context: Context, path: String, title: String, size: Long): Boolean {
    val file = CleanFolder()
    file.path = path
    file.size = size
    file.title = title
    return MainDB.getInstance(context).folderDao.insert(file) > 0
}

fun removeFolder(context: Context, id: Long): Boolean {
    return MainDB.getInstance(context).folderDao.delete(id) > 0
}
