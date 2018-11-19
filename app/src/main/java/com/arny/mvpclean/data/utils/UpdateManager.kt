package com.arny.mvpclean.data.utils


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.arny.mvpclean.data.repository.main.MainRepositoryImpl
import com.arny.mvpclean.data.utils.DateTimeUtils.getDateTime


class UpdateManager : BroadcastReceiver() {
    private val repository = MainRepositoryImpl()
    companion object {
        const val INTENT_UPDATE_MANAGER_STATE = "intent_update_manager_state"
        const val INTENT_UPDATE_MANAGER_STATE_UPDATED = "intent_update_manager_state_updated"
    }

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent) {
        println("LOG UpdateManager onReceive time ${getDateTime()}")
        val stopwatch = Stopwatch()
        stopwatch.start()
        mainThreadObservable(
                repository.getList()
                        .map { folderFiles ->
                            for (folderFile in folderFiles) {
                                val rem = FileUtils.cleanDirectory(folderFile.path)
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
