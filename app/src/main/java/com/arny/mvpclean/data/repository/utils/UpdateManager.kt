package com.arny.mvpclean.data.repository.utils


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.utils.DateTimeUtils.getDateTime
import com.arny.arnylib.utils.Stopwatch
import com.arny.arnylib.utils.ToastMaker
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.data.repository.main.getList
import java.io.File


class UpdateManager : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("LOG UpdateManager onReceive time ${getDateTime()}")
        val stopwatch = Stopwatch()
        stopwatch.start()
        Utility.mainThreadObservable(
                getList(context)
                        ?.map { folderFiles ->
                            for (folderFile in folderFiles) {
                                val rem = FileUtils.deleteFile(File(folderFile.path))
                                if (!rem) {
                                    stopwatch.stop()
                                    return@map false
                                }
                            }
                            stopwatch.stop()
                            true
                        }
        ).subscribe({ aBoolean ->
            println("UpdateManager onReceive $aBoolean")
            if (aBoolean) {
                ToastMaker.toastSuccess(context, "Файлы не удалены")
            } else {
                ToastMaker.toastError(context, "Файлы не удалены")
            }
        }, {
            println("UpdateManager onReceive error ${it.message}")
            ToastMaker.toastError(context, "Файлы не удалены:${it.message}")
        })
    }
}
