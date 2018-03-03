package com.arny.mvpclean.presenter.base

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.arny.arnylib.utils.ToastMaker

abstract class BaseMvpActivity<in V : BaseMvpView, T : BaseMvpPresenter<V>> : AppCompatActivity(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    override fun getContext(): Context = this

    protected abstract var mPresenter: T

    override fun toastError(error: String?) {
        ToastMaker.toastError(getContext(), error)
    }

    override fun toastSuccess(message: String?) {
        ToastMaker.toastSuccess(getContext(), message)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }
}