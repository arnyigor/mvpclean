package com.arny.mvpclean;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import com.facebook.stetho.Stetho;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
public class CleanApp extends DaggerApplication {

	private static Context context;

	public static Context getContext() {
		return context;
	}

	@Override
	protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
		return DaggerAppComponent.builder().application(this).build();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}


	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		Stetho.initializeWithDefaults(this);
	}
}
