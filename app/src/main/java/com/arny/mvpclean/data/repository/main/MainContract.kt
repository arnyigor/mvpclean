package com.arny.mvpclean.data.repository.main

import com.arny.arnylib.utils.Stopwatch
import io.reactivex.Observable

interface MainContract {
    fun clearFolders(stopwatch: Stopwatch): Observable<Boolean>?
}