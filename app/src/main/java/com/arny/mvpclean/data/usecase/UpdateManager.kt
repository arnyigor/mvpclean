package com.arny.mvpclean.data.usecase


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.utils.DateTimeUtils.getDateTime
import com.arny.arnylib.utils.Stopwatch
import com.arny.arnylib.utils.ToastMaker
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.data.repository.main.MainRepository
import java.io.File


class UpdateManager : BroadcastReceiver() {
    private val repository = MainRepository()
    companion object {
        const val INTENT_UPDATE_MANAGER_STATE = "intent_update_manager_state"
        const val INTENT_UPDATE_MANAGER_STATE_UPDATED = "intent_update_manager_state_updated"
    }

    override fun onReceive(context: Context, intent: Intent) {
        println("LOG UpdateManager onReceive time ${getDateTime()}")
        val stopwatch = Stopwatch()
        stopwatch.start()
        Utility.mainThreadObservable(
                repository.getList()
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
                ToastMaker.toastSuccess(context, "Файлы удалены")
                val updateIntent = Intent(INTENT_UPDATE_MANAGER_STATE)
                updateIntent.addCategory(Intent.CATEGORY_DEFAULT)
                updateIntent.putExtra(INTENT_UPDATE_MANAGER_STATE_UPDATED, true)
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
            } else {
                ToastMaker.toastError(context, "Файлы не удалены")
            }
        }, {
            println("UpdateManager onReceive error ${it.message}")
            ToastMaker.toastError(context, "Файлы не удалены:${it.message}")
        })
    }
}
