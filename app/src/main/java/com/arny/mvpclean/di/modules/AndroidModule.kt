package com.arny.mvpclean.di.modules

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.support.annotation.NonNull
import com.arny.mvpclean.data.repository.main.MainDB
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/**
 * A module for Android-specific dependencies which require a [android.content.Context] or [ ] to create.
 */
@Module
class AndroidModule(private val application: Application) {
    @Provides
    @Singleton
    @NonNull
    fun provideApplicationContext(): Context = application

    @Provides
    @Singleton
    @NonNull
    fun providesAppDatabase(context: Context): MainDB =
            Room.databaseBuilder(context, MainDB::class.java, "Clean.db")
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides
    @Singleton
    @Named("something")
    fun provideSomething(): String = "something"

}