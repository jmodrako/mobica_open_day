package com.mobica.openday.util;

import android.util.Log;

/**
 * Mobica Open Day 2015
 *
 * @author jacek.modrakowski@mobica.com
 */
public class Logger {

	private static final boolean IS_ENABLED = true;
	private static final String TAG = "logger_wear";

	public static void l(String msg) {
		if (IS_ENABLED) {
			Log.d(TAG, msg);
		}
	}
}
