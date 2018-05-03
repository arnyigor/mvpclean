package com.arny.mvpclean.presenter.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import androidx.content.systemService
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.utils.DroidUtils
import com.arny.arnylib.utils.Stopwatch
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.data.usecase.getTimeDiff
import com.arny.mvpclean.data.usecase.UpdateManager
import com.arny.mvpclean.presenter.base.BaseMvpPresenterImpl
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.File
import java.util.*
import com.arny.arnylib.utils.DateTimeUtils
import com.arny.mvpclean.data.repository.main.MainRepository


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(), MainContract.Presenter {
    private var folders: ArrayList<CleanFolder> = ArrayList<CleanFolder>()
    private val repository = MainRepository()
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            Log.d("MainPresenter", "intent: ${DroidUtils.dumpIntent(intent)}")
            val updated = intent?.getBooleanExtra(UpdateManager.INTENT_UPDATE_MANAGER_STATE_UPDATED, false) ?: false
            if (updated) {
                loadList()
            }
        }
    }

    override fun setSchedule(scheduleData: ScheduleData?) {
        val work = scheduleData?.isWork ?: false
        val alarmManager = repository.context.systemService<AlarmManager>()
        val pi = PendingIntent.getBroadcast(repository.context, 0,
                Intent(repository.context, UpdateManager::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        if (work) {
            val time = scheduleData?.time
            val calendar = Calendar.getInstance()
            val diff = time?.let { getTimeDiff(it, calendar.timeInMillis) } ?: 0
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + diff, pi)
            val triggerTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.nextAlarmClock.triggerTime
            } else {
                val string = Settings.System.getString(repository.context.contentResolver, Settings.System.NEXT_ALARM_FORMATTED)
                println("string:$string")
                val timeStringToLong = DateTimeUtils.convertTimeStringToLong(string)
                timeStringToLong//TODO проверить
            }
            val dateTime = DateTimeUtils.getDateTime(triggerTime)
            println("dateTime:$dateTime")
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
            folders[position].id?.let { repository.removeFolder(it) } ?: false
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

    private fun checkPathNotFound(list: MutableList<CleanFolder>, path: String): Boolean {
        return list.find { it.path == path } == null
    }

    override fun addFolder(folder: File) {
        Utility.mainThreadObservable(Observable.zip(repository.getList(), Observable.fromCallable { folder.absolutePath }, BiFunction<MutableList<CleanFolder>, String, Boolean> { list, path -> checkPathNotFound(list, path) })
                .flatMap({ check ->
                    if (check) {
                        Observable.fromCallable({
                            repository.addFolderToClean(folder.absolutePath)
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
        Utility.mainThreadObservable(repository.getList()
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

    override fun attachView(mvpView: MainContract.View) {
        super.attachView(mvpView)
        val filter = IntentFilter(UpdateManager.INTENT_UPDATE_MANAGER_STATE)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        LocalBroadcastManager.getInstance(repository.context).registerReceiver(broadcastReceiver, filter)
    }

    override fun detachView() {
        super.detachView()
        LocalBroadcastManager.getInstance(repository.context).unregisterReceiver(broadcastReceiver)
    }

}
