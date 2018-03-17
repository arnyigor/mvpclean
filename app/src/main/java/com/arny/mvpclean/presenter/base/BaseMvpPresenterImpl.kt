package com.arny.mvpclean.presenter.base

import android.content.Context
import com.arny.mvpclean.CleanApp

open class BaseMvpPresenterImpl<V : BaseMvpView> : BaseMvpPresenter<V> {
    protected var context: Context =  CleanApp.getContext()
    protected var mView: V? = null

    override fun attachView(mvpView: V) {
        mView = mvpView
    }

    override fun detachView() {
        mView = null
    }

}