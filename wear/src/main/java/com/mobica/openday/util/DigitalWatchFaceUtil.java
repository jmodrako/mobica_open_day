/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobica.openday.util;

import android.net.Uri;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import com.mobica.mobilewearcommons.data.FaceData;

public abstract class DigitalWatchFaceUtil {

	public static final String PATH_WITH_FEATURE = "/mobica_face_conf/clock";

	public interface FetchConfigDataMapCallback {
		void onConfigDataMapFetched(FaceData faceData);
	}

	public static void fetchConfigDataMap(final GoogleApiClient client,
										  final FetchConfigDataMapCallback callback) {
		Wearable.NodeApi.getLocalNode(client).setResultCallback(
				new ResultCallback<NodeApi.GetLocalNodeResult>() {
					@Override
					public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
						String localNode = getLocalNodeResult.getNode().getId();
						Uri uri = new Uri.Builder()
								.scheme("wear")
								.path(DigitalWatchFaceUtil.PATH_WITH_FEATURE)
								.authority(localNode)
								.build();
						Wearable.DataApi.getDataItem(client, uri)
								.setResultCallback(new DataItemResultCallback(callback));
					}
				}
		);
	}

	/**
	 * Overwrites (or sets, if not present) the keys in the current config {@link DataItem} with
	 * the ones appearing in the given {@link DataMap}. If the config DataItem doesn't exist,
	 * it's created.
	 * <p/>
	 * It is allowed that only some of the keys used in the config DataItem appear in
	 * {@code configKeysToOverwrite}. The rest of the keys remains unmodified in this case.
	 */
	public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
													final FaceData configKeysToOverwrite) {

		DigitalWatchFaceUtil.fetchConfigDataMap(googleApiClient,
				new FetchConfigDataMapCallback() {
					@Override
					public void onConfigDataMapFetched(FaceData currentConfig) {
						DataMap overwrittenConfig = new DataMap();
						overwrittenConfig.putAll(DataMap.fromBundle(currentConfig.toBundle()));
						overwrittenConfig.putAll(DataMap.fromBundle(configKeysToOverwrite.toBundle()));
						DigitalWatchFaceUtil.putConfigDataItem(googleApiClient, overwrittenConfig);
					}
				}
		);
	}

	/**
	 * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
	 * If the config DataItem doesn't exist, it's created.
	 */
	public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
		PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
		DataMap configToPut = putDataMapRequest.getDataMap();
		configToPut.putAll(newConfig);
		Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
				.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
					@Override
					public void onResult(DataApi.DataItemResult dataItemResult) {
						Logger.l("DigitalWatchFaceUtil.putConfigDataItem");
					}
				});
	}

	private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

		private final FetchConfigDataMapCallback mCallback;

		public DataItemResultCallback(FetchConfigDataMapCallback callback) {
			mCallback = callback;
		}

		@Override
		public void onResult(DataApi.DataItemResult dataItemResult) {
			Logger.l("DataItemResultCallback.onResult");

			if (dataItemResult.getStatus().isSuccess()) {
				Logger.l("DataItemResultCallback.onResult.success");

				if (dataItemResult.getDataItem() != null) {
					Logger.l("DataItemResultCallback.onResult.success.data_available");

					DataItem configDataItem = dataItemResult.getDataItem();
					DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
					DataMap config = dataMapItem.getDataMap();
					FaceData faceData = FaceData.fromDataMap(config);

					Logger.l("DataItemResultCallback.onResult.success.data_available.face_data: " + faceData);

					mCallback.onConfigDataMapFetched(faceData);
				} else {
					Logger.l("DataItemResultCallback.onResult.success.default_face");

					mCallback.onConfigDataMapFetched(FaceData.createAnalogData());
				}
			}
		}
	}

	private DigitalWatchFaceUtil() {
	}
}
