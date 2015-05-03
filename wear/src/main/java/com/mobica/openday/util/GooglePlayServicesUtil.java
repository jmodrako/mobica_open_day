package com.mobica.openday.util;

import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * Mobica Open Day 2015
 *
 * @author jacek.modrakowski@mobica.com
 */
public abstract class GooglePlayServicesUtil {

	public static final int GOOGLE_API_CONNECTION_TIMEOUT_SECONDS = 30;

	public static GoogleApiClient createSimpleWearClient(Context context) {
		return new GoogleApiClient.Builder(context)
				.addApi(Wearable.API)
				.build();
	}

	public static GoogleApiClient createWearClientWithListeners(
			Context context,
			GoogleApiClient.ConnectionCallbacks connectionCallbacks,
			GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {

		return new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(connectionCallbacks)
				.addOnConnectionFailedListener(connectionFailedListener)
				.addApi(Wearable.API)
				.build();
	}

	public static ConnectionResult connectToSimpleWearApiWithTimeout(Context context, int timeOutSecond) {
		return createSimpleWearClient(context).blockingConnect(timeOutSecond, TimeUnit.SECONDS);
	}

	public static ConnectionResult connectToSimpleWearApiWithTimeout(GoogleApiClient apiClient, int timeOutSecond) {
		return apiClient.blockingConnect(timeOutSecond, TimeUnit.SECONDS);
	}

	private GooglePlayServicesUtil() {
		// Prevents object creation.
	}
}
