package com.arny.mvpclean.presenter.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.utils.Stopwatch
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.CleanApp
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.repository.main.addFolderToClean
import com.arny.mvpclean.data.repository.main.getList
import com.arny.mvpclean.data.repository.main.removeFolder
import com.arny.mvpclean.presenter.base.BaseMvpPresenterImpl
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.File
import android.os.SystemClock
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.content.systemService
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.data.repository.utils.UpdateManager
import com.arny.mvpclean.data.repository.utils.getTimeDiff
import java.util.*


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(), MainContract.Presenter {
    override fun setSchedule(scheduleData: ScheduleData?) {
        val work = scheduleData?.isWork ?: false
        val alarmManager = mContext.systemService<AlarmManager>()
        val pi = PendingIntent.getBroadcast(mContext, 0,
                Intent(mContext, UpdateManager::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        if (work) {
            val time = scheduleData?.time
            val calendar = Calendar.getInstance()
            val diff = time?.let { getTimeDiff(it, calendar.timeInMillis) } ?: 0
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + diff, pi)
            // Restart alarm if device is rebooted
//            val receiver = ComponentName(mContext, BootReceiver::class.java)
//            val pm = mContext.packageManager
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP)
        } else {
            alarmManager.cancel(pi)
        }

    }

    override fun updateFoldersSize(list: ArrayList<CleanFolder>) {
        Utility.mainThreadObservable(Observable.fromCallable { list }
                .map {
                    for (cleanFolder in it) {
                        cleanFolder.title = FileUtils.getFilenameWithExtention(cleanFolder.path)
                        cleanFolder.size = FileUtils.getFolderSize(File(cleanFolder.path))
                    }
                    it
                })
                .subscribe({
                    this.folders = it
                    mView?.updateList()
                    calcTotalSize()
                })


    }

    override fun cleanFolders() {
        val stopwatch = Stopwatch()
        stopwatch.start()
        Utility.mainThreadObservable(
                getList(mContext)
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
            if (aBoolean) {
                mView?.toastSuccess("Файлы удалены за ${stopwatch.elapsedTimeMili} мс")
                loadList()
            } else {
                mView?.showError("Файлы не удалены")
            }
        }, {
            it.printStackTrace()
            mView?.showError("Файлы не удалены:${it.message}")
        })
    }

    override fun removeFolderItem(position: Int, list: ArrayList<CleanFolder>) {
        this.folders = list
        Utility.mainThreadObservable(Observable.fromCallable {
            folders[position].id?.let { removeFolder(mContext, it) } ?: false
        })
                .subscribe({ del ->
                    if (del) {
                        this.folders.removeAt(position)
                        mView?.updateList()
                        calcTotalSize()
                    }
                }, {
                    it.printStackTrace()
                    mView?.showError(it.message)
                })
    }

    private var mContext: Context = mView?.getContext() ?: CleanApp.getContext()
    private var folders: ArrayList<CleanFolder> = ArrayList<CleanFolder>()


    private fun checkPathNotFound(list: MutableList<CleanFolder>, path: String): Boolean {
        return list.find { it.path == path } == null
    }

    override fun addFolder(folder: File) {
        Utility.mainThreadObservable(Observable.zip(getList(mContext), Observable.fromCallable { folder.absolutePath }, BiFunction<MutableList<CleanFolder>, String, Boolean> { list, path -> checkPathNotFound(list, path) })
                .flatMap({ check ->
                    if (check) {
                        Observable.fromCallable({
                            addFolderToClean(mContext, folder.absolutePath)
                        })
                    } else {
                        Handler(Looper.getMainLooper()).post({
                            mView?.toastError("Папка уже существует в списке")
                        })
                        Observable.fromCallable { false }
                    }
                })
                .map { save ->
                    if (!save) {
                        Handler(Looper.getMainLooper()).post({
                            mView?.showError("Папка не добавлена")
                        })
                    }
                    save
                }
        )
                .doOnSubscribe {
                    mView?.updateInfo("Добавление папки")
                }
                .subscribe({ save ->
                    if (save) {
                        mView?.clearList()
                        loadList()
                    } else {
                        calcTotalSize()
                    }
                }, {
                    it.printStackTrace()
                    mView?.showError(it.message)
                })
    }

    override fun calcTotalSize() {
        val total: Long = folders.map { it.size }.sum()
        val fileSize = FileUtils.formatFileSize(total, 3)
        mView?.showTotalSize("Общий размер $fileSize")
        mView?.updateBtn(folders.size != 0)
    }

    override fun loadList() {
        Utility.mainThreadObservable(getList(mContext)
                ?.map { it -> it as ArrayList })
                .subscribe({
                    this.folders = it
                    mView?.clearList()
                    mView?.showList(folders)
                    calcTotalSize()
                }, {
                    it.printStackTrace()
                    mView?.showError(it.message)
                })
    }

}