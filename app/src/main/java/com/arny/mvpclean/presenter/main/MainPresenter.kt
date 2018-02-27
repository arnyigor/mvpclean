package com.arny.mvpclean.presenter.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.arny.arnylib.files.FileUtils
import com.arny.arnylib.utils.Utility
import com.arny.mvpclean.CleanApp
import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.repository.main.addFolderToClean
import com.arny.mvpclean.data.repository.main.getList
import com.arny.mvpclean.presenter.base.BaseMvpPresenterImpl
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import java.io.File


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(), MainContract.Presenter {
    private var mContext: Context = CleanApp.getContext()
    private var files: List<CleanFolder> = ArrayList<CleanFolder>()

    override fun addFolder(folder: File) {
        Utility.mainThreadObservable(Observable.fromCallable({
            addFolderToClean(mContext, folder.absolutePath,
                    FileUtils.getFilenameWithExtention(folder.absolutePath),
                    FileUtils.getFolderSize(File(folder.absolutePath)))
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
                    }
                }, {
                    it.printStackTrace()
                    mView?.showError(it.message)
                })
    }

    override fun calcTotalSize() {
        val total: Long = files.map { it.size }.sum()
        val fileSize = FileUtils.formatFileSize(total, 3)
        mView?.showTotalSize("Общий размер $fileSize")
    }

    override fun loadList() {
        Utility.mainThreadObservable(getList(mContext))
                .doOnSubscribe { mView?.showMessage("Загрузка списка") }
                .subscribe({
                    this.files = it
                    mView?.clearList()
                    mView?.showList(it)
                }, {
                    it.printStackTrace()
                    mView?.showError(it.message)
                })
    }
}