package com.arny.mvpclean.data.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.arny.mvpclean.data.repository.main.MainRepositoryImpl
import com.arny.mvpclean.data.utils.Stopwatch
import com.arny.mvpclean.data.utils.observeOnMain


class ClearFolderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val repository = MainRepositoryImpl()
    @SuppressLint("CheckResult")
    override fun doWork(): Result {
        return try {
            val stopwatch = Stopwatch()
            stopwatch.start()
            repository.clearFolders(stopwatch)?.subscribe({
                Log.i(ClearFolderWorker::class.java.simpleName, "doWork: time ${stopwatch.formatTime(3)} thread:" + Thread.currentThread().name)
            }, {
                it.printStackTrace()
            })
            Result.SUCCESS
        } catch (throwable: Throwable) {
            Result.FAILURE
        }
    }
}