package com.arny.mvpclean

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.support.multidex.MultiDex
import com.arny.mvpclean.di.components.ApplicationComponent
import com.arny.mvpclean.di.components.DaggerApplicationComponent
import com.arny.mvpclean.di.modules.AndroidModule
import com.facebook.stetho.Stetho
import javax.inject.Inject

class CleanApp : Application() {
    companion object {
        //platformStatic allow access it from java code
        @JvmStatic lateinit var applicationComponent: ApplicationComponent
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }


    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
        applicationComponent.inject(this)
        Stetho.initializeWithDefaults(this)
    }
}
