package com.arny.mvpclean.presenter.base

import android.content.Context
import android.support.annotation.StringRes

interface BaseMvpView {
    fun getContext(): Context
    fun showError(error: String?)
    fun toastError(error: String?)
    fun toastSuccess(message: String?)
}
