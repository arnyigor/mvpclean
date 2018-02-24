package com.arny.mvpclean;

import android.app.Application;
import android.content.Context;
public class CleanApp extends Application {

	private static Context context;

	public static Context getContext() {
		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}
}
