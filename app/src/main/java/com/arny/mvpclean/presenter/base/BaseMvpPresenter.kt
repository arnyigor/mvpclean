package com.arny.mvpclean.presenter.base

interface BaseMvpPresenter<in V : BaseMvpView> {

    fun attachView(mvpView: V)

    fun detachView()

}