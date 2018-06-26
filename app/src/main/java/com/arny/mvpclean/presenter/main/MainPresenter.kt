package com.arny.mvpclean.presenter.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.util.TimeUtils
import androidx.work.*
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.presenter.base.BaseMvpPresenterImpl
import com.arny.arnylib.utils.DroidUtils
import com.arny.arnylib.utils.Stopwatch
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.data.repository.main.MainRepository
import com.arny.mvpclean.data.utils.UpdateManager
import com.arny.mvpclean.data.utils.getTimeDiff
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.File
import java.util.*
import com.arny.mvpclean.data.worker.ClearFolderWorker
import java.util.concurrent.TimeUnit


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(), MainContract.Presenter {
    private var folders: ArrayList<CleanFolder> = ArrayList()

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
//        val work = scheduleData?.isWork ?: false
//        val alarmManager = repository.getContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager;
//        val pi = PendingIntent.getBroadcast(repository.getContext(), 0,
//                Intent(repository.getContext(), UpdateManager::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
//        if (work) {
//            val time = scheduleData?.time
//            val calendar = Calendar.getInstance()
//            val diff = time?.let { getTimeDiff(it, calendar.timeInMillis) } ?: 0
//            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + diff, pi)
//        } else {
//            alarmManager.cancel(pi)
//        }
        val work = scheduleData?.isWork ?: false
        if (work) {
            val myConstrains = Constraints.Builder()
                    .setRequiresStorageNotLow(false)
                    .setRequiresCharging(false)
                    .build()
            val managerTag = "12345"
            val periodicWorkRequest = PeriodicWorkRequestBuilder<ClearFolderWorker>(1, TimeUnit.MINUTES)
                    .setConstraints(myConstrains)
                    .addTag(managerTag)
                    .build()
            repository.setPrefString("work_manager_tag", managerTag)
            WorkManager.getInstance()?.enqueue(periodicWorkRequest)
        } else {
            val tag = repository.getPrefString("work_manager_tag")
            tag?.let { WorkManager.getInstance()?.cancelAllWorkByTag(it) }
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
                .subscribe {
                    this.folders = it
                    mView?.updateList()
                    calcTotalSize()
                }
    }

    override fun cleanFolders() {
        val stopwatch = Stopwatch()
        stopwatch.start()
        Utility.mainThreadObservable(
                repository.clearFolders(stopwatch)
        ).subscribe({ aBoolean ->
            if (aBoolean) {
                mView?.toastSuccess("Файлы удалены за ${stopwatch.elapsedTimeMili} мс")
                loadList()
            } else {
                mView?.toastError("Файлы не удалены")
            }
        }, {
            it.printStackTrace()
            mView?.toastError("Файлы не удалены:${it.message}")
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
                    mView?.toastError(it.message)
                })
    }

    private fun checkPathNotFound(list: MutableList<CleanFolder>, path: String): Boolean {
        return list.find { it.path == path } == null
    }

    override fun addFolder(folder: File) {
        Utility.mainThreadObservable(Observable.zip(repository.getList(), Observable.fromCallable { folder.absolutePath }, BiFunction<MutableList<CleanFolder>, String, Boolean> { list, path -> checkPathNotFound(list, path) })
                .flatMap { check ->
                    if (check) {
                        Observable.fromCallable {
                            repository.addFolderToClean(folder.absolutePath)
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            mView?.toastError("Папка уже существует в списке")
                        }
                        Observable.fromCallable { false }
                    }
                }
                .map { save ->
                    if (!save) {
                        Handler(Looper.getMainLooper()).post {
                            mView?.toastError("Папка не добавлена")
                        }
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
                    mView?.toastError(it.message)
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
                    mView?.toastError(it.message)
                })
    }

    override fun attachView(mvpView: MainContract.View) {
        super.attachView(mvpView)
        val filter = IntentFilter(UpdateManager.INTENT_UPDATE_MANAGER_STATE)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        LocalBroadcastManager.getInstance(repository.getContext()).registerReceiver(broadcastReceiver, filter)
    }

    override fun detachView() {
        super.detachView()
        LocalBroadcastManager.getInstance(repository.getContext()).unregisterReceiver(broadcastReceiver)
    }

}
