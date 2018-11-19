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
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
            val workerTag = "12345"
            val current = Calendar.getInstance().timeInMillis
            val time = scheduleData?.time
            val currentDate = DateTimeUtils.getDateTime(current, "yyyy-MM-dd")
            val formattedDateTime = "$currentDate $time"
            val expected = DateTimeUtils.convertTimeStringToLong(formattedDateTime, "yyyy-MM-dd HH:mm")
            if (expected != -1L) {
                val timeDiff = DateTimeUtils.getTimeDiff(current, expected)
                Log.i(MainPresenter::class.java.simpleName, "setSchedule: delay:$timeDiff");
                val workRequestBuilder = OneTimeWorkRequestBuilder<ClearFolderWorker>()
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .addTag(workerTag)
                        .build()
                //            val periodicWorkRequest = PeriodicWorkRequestBuilder<ClearFolderWorker>(1, TimeUnit.MINUTES)
//                    .setConstraints(myConstrains)
//                    .addTag(workerTag)
//                    .build()
                repository.setPref(Consts.Global.WORK_MANAGER_TAG, workerTag)
                WorkManager.getInstance().enqueue(workRequestBuilder)
            }
        } else {
            val tag = repository.getPrefString(Consts.Global.WORK_MANAGER_TAG)
            tag?.let { WorkManager.getInstance().cancelAllWorkByTag(it) }
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
