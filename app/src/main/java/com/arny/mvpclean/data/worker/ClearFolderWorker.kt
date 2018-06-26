package com.arny.mvpclean.data.worker

import androidx.work.Worker
import com.arny.arnylib.utils.Stopwatch
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.data.repository.main.MainRepository


class ClearFolderWorker : Worker() {
    private val repository = MainRepository()
    override fun doWork(): Result {
        try {
            val stopwatch = Stopwatch()
            stopwatch.start()
            Utility.IOThreadObservable(repository.clearFolders(stopwatch)).subscribe()
            return Result.SUCCESS
        } catch (throwable: Throwable) {
            return Result.FAILURE
        }
    }
}