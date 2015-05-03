package com.mobica.openday.service;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;
import com.mobica.openday.util.Logger;

/**
 * Mobica Open Day 2015
 *
 * @author jacek.modrakowski@mobica.com
 */
public class FaceDataReceiverService extends WearableListenerService {

	private GoogleApiClient googleApiClient;

	@Override public void onDataChanged(DataEventBuffer dataEvents) {
		super.onDataChanged(dataEvents);
//		Logger.l("FaceDataReceiverService.onDataChanged, events count: " + dataEvents.getCount());
//
//		final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
//
//		if (googleApiClient == null) {
//			googleApiClient = GooglePlayServicesUtil.createSimpleWearClient(this);
//		}
//
//		ConnectionResult connectionResult = GooglePlayServicesUtil
//				.connectToSimpleWearApiWithTimeout(googleApiClient,
//						GooglePlayServicesUtil.GOOGLE_API_CONNECTION_TIMEOUT_SECONDS);
//
//		if (connectionResult.isSuccess()) {
//			Logger.l("FaceDataReceiverService.GoogleApiClient connected!");
//
//			for (DataEvent event : events) {
//				Logger.l("FaceDataReceiverService.onDataChanged building data!");
//
//				DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
//				FaceData faceData = FaceData.fromDataMap(dataMap);
//
//				DigitalWatchFaceUtil.overwriteKeysInConfigDataMap(googleApiClient, faceData);
//			}
//		} else {
//			Logger.l("FaceDataReceiverService.GoogleApiClient can't connect!");
//		}
	}

	@Override public void onCreate() {
		super.onCreate();
		Logger.l("FaceDataReceiverService.onCreate");
	}

	@Override public void onDestroy() {
		super.onDestroy();
		Logger.l("FaceDataReceiverService.onDestroy");
	}

	@Override public void onMessageReceived(MessageEvent messageEvent) {
		super.onMessageReceived(messageEvent);
		Logger.l("FaceDataReceiverService.onMessageReceived");
	}

	@Override public void onPeerConnected(Node peer) {
		super.onPeerConnected(peer);
		Logger.l("FaceDataReceiverService.onPeerConnected");
	}

	@Override public void onPeerDisconnected(Node peer) {
		super.onPeerDisconnected(peer);
		Logger.l("FaceDataReceiverService.onPeerDisconnected");
	}
}
