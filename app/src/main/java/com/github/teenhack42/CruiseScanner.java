package com.github.teenhack42;

import android.app.Application;
import android.content.Context;

/**
 * Created by grant on 10/2/18.
 */

public class CruiseScanner extends Application {
	private static Context context;

	public void onCreate() {
		super.onCreate();
		CruiseScanner.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return CruiseScanner.context;
	}
}
