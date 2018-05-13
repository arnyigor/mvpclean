package com.arny.mvpclean.di.components

import android.content.Context
import com.arny.mvpclean.CleanApp
import com.arny.mvpclean.data.repository.main.MainDB
import com.arny.mvpclean.data.repository.main.MainRepository
import com.arny.mvpclean.data.utils.UpdateManager
import com.arny.mvpclean.di.modules.AndroidModule
import com.arny.mvpclean.presenter.main.MainPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AndroidModule::class)])
interface ApplicationComponent {
    fun inject(target: CleanApp)
    fun inject(target: UpdateManager)
    fun inject(target: MainPresenter)
    fun inject(target: MainRepository)
    fun getContext(): Context
    fun getDb(): MainDB
}