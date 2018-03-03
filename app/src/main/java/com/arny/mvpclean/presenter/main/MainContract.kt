package com.arny.mvpclean.presenter.main

import com.arny.mvpclean.data.models.CleanFolder
import com.arny.mvpclean.data.models.ScheduleData
import com.arny.mvpclean.presenter.base.BaseMvpPresenter
import com.arny.mvpclean.presenter.base.BaseMvpView
import java.io.File

object MainContract {
    interface View : BaseMvpView {
        fun showList(list: MutableList<CleanFolder>)
        fun updateList()
        fun clearList()
        fun showTotalSize(size: String)
        fun showAddDialog()
        fun updateInfo(text: String)
        fun showMessage(message: String)
        fun updateBtn(enable: Boolean)
    }

    interface Presenter : BaseMvpPresenter<View> {
        fun setSchedule(scheduleData: ScheduleData?)
        fun loadList()
        fun removeFolderItem(position: Int, folders: ArrayList<CleanFolder>)
        fun calcTotalSize()
        fun addFolder(folder: File)
        fun cleanFolders()
        fun updateFoldersSize(folders: ArrayList<CleanFolder>)
    }
}