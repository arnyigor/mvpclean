package com.arny.mvpclean.presenter.main

import com.arny.mvpclean.data.models.CleanFolder
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
    }

    interface Presenter : BaseMvpPresenter<View> {
        fun loadList()
        fun calcTotalSize()
        fun addFolder(folder: File)
    }
}