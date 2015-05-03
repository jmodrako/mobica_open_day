package com.mobica.openday.android.util;

import android.util.Log;

/**
 * Mobica Open Day 2015
 *
 * @author jacek.modrakowski@mobica.com
 */
public class Logger {

	private static final boolean IS_ENABLED = true;
	private static final String TAG = "logger_app";

	public static void l(String msg) {
		if (IS_ENABLED) {
			Log.d(TAG, msg);
		}
	}
}
