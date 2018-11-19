package com.arny.mvpclean.presenter.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.data.repository.main.Consts
import com.arny.mvpclean.data.repository.main.MainRepositoryImpl
import com.arny.mvpclean.data.utils.*
import com.arny.mvpclean.data.worker.ClearFolderWorker
import com.arny.mvpclean.presenter.base.BaseMvpPresenterImpl
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(), MainContract.Presenter {
    private var folders: ArrayList<CleanFolder> = ArrayList()
    private val repository = MainRepositoryImpl.instance
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            Log.d("MainPresenter", "intent: ${intent.dump()}")
            val updated = intent?.getBooleanExtra(UpdateManager.INTENT_UPDATE_MANAGER_STATE_UPDATED, false) ?: false
            if (updated) {
                loadList()
            }
        }
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    override fun setSchedule(scheduleData: ScheduleData?) {
        val data = scheduleData
        val work = data?.isWork ?: false
        mView?.setSchelude("Расписание:Время-${scheduleData?.time} Периодически:${scheduleData?.isRepeat}")
        if (data != null && work) {
            val tag = UUID.randomUUID().toString()
            val current = System.currentTimeMillis()
            val repeat = data.isRepeat
            val periodType = data.periodType
            val time = data.time
            val currentDate = DateTimeUtils.getDateTime(current, "yyyy-MM-dd")
            val formattedDateTime = "$currentDate $time"
            var expected = DateTimeUtils.convertTimeStringToLong(formattedDateTime, "yyyy-MM-dd HH:mm")
            val timeDiff = DateTimeUtils.getTimeDiff(current, expected)
            val diff = DateTimeUtils.formatTime(timeDiff)
            val msg = "Выполняем через $diff"
            mView?.toastSuccess(msg)
            if (repeat && expected == -1L) {
                expected = 0
            }
            if (expected != -1L) {
                val workRequest: WorkRequest
                workRequest = if (!repeat) {
                    OneTimeWorkRequestBuilder<ClearFolderWorker>()
                            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                            .addTag(tag)
                            .build()
                } else {
                    var timeunit: TimeUnit = TimeUnit.MINUTES
                    val period: Long?
                    when (periodType) {
                        0 -> {
                            period = 15
                            timeunit = TimeUnit.MINUTES
                        }
                        1 -> {
                            period = 30
                            timeunit = TimeUnit.MINUTES
                        }
                        2 -> {
                            period = 1
                            timeunit = TimeUnit.HOURS
                        }
                        3 -> {
                            period = 3
                            timeunit = TimeUnit.HOURS
                        }
                        4 -> {
                            period = 6
                            timeunit = TimeUnit.HOURS
                        }
                        5 -> {
                            period = 9
                            timeunit = TimeUnit.HOURS
                        }
                        6 -> {
                            period = 12
                            timeunit = TimeUnit.HOURS
                        }
                        7 -> {
                            period = 18
                            timeunit = TimeUnit.HOURS
                        }
                        8 -> {
                            period = 24
                            timeunit = TimeUnit.HOURS
                        }
                        9 -> {
                            period = 2
                            timeunit = TimeUnit.DAYS
                        }
                        10 -> {
                            period = 3
                            timeunit = TimeUnit.DAYS
                        }
                        11 -> {
                            period = 4
                            timeunit = TimeUnit.DAYS
                        }
                        12 -> {
                            period = 5
                            timeunit = TimeUnit.DAYS
                        }
                        13 -> {
                            period = 6
                            timeunit = TimeUnit.DAYS
                        }
                        14 -> {
                            period = 7
                            timeunit = TimeUnit.DAYS
                        }
                        else -> {
                            period = null
                        }
                    }
                    if (period == null) return
                    PeriodicWorkRequestBuilder<ClearFolderWorker>(period, timeunit)
                            .setPeriodStartTime(timeDiff, TimeUnit.MILLISECONDS)
                            .addTag(tag)
                            .build()
                }
                repository.setPref(Consts.Global.WORK_MANAGER_TAG, tag)
                WorkManager.getInstance().enqueue(workRequest)
            }
        } else {
            val tag = repository.getPrefString(Consts.Global.WORK_MANAGER_TAG)
            if (tag != null) {
                WorkManager.getInstance().cancelAllWorkByTag(tag)
                mView?.setSchelude("")
                mView?.toastSuccess("Очистка отменена")
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun updateFoldersSize(folders: ArrayList<CleanFolder>) {
        Observable.fromCallable { folders }
                .map {
                    for (cleanFolder in it) {
                        cleanFolder.title = FileUtils.getFilenameWithExtention(cleanFolder.path)
                        cleanFolder.size = FileUtils.getFolderSize(File(cleanFolder.path))
                    }
                    it
                }.observeOnMain()
                .subscribe {
                    this.folders = it
                    mView?.updateList()
                    calcTotalSize()
                }
    }

    @SuppressLint("CheckResult")
    override fun cleanFolders() {
        val stopwatch = Stopwatch()
        stopwatch.start()
        repository.clearFolders(stopwatch)
                ?.observeOnMain()?.subscribe({ aBoolean ->
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

    @SuppressLint("CheckResult")
    override fun removeFolderItem(position: Int, list: ArrayList<CleanFolder>) {
        this.folders = list
        Observable.fromCallable {
            folders[position].id?.let { repository.removeFolder(it) } ?: false
        }.observeOnMain()
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

    @SuppressLint("CheckResult")
    override fun addFolder(folder: File) {
        mainThreadObservable(Observable.zip(repository.getList(), Observable.fromCallable { folder.absolutePath }, BiFunction<MutableList<CleanFolder>, String, Boolean> { list, path -> checkPathNotFound(list, path) })
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
        val time = repository.getPrefLong(Consts.Global.WORK_MANAGER_LAST_CLEAN_TIME, 0)
        val lasTime = DateTimeUtils.getDateTime(time, "yyyy-MM-dd HH:mm:ss")
        mView?.showLastCleanTime("Последняя очистка $lasTime")
        mView?.updateBtn(folders.size != 0)
    }

    @SuppressLint("CheckResult")
    override fun loadList() {
        repository.getList().observeOnMain()
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
