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

package com.mobica.openday;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.*;
import com.mobica.mobilewearcommons.data.FaceData;
import com.mobica.openday.util.DigitalWatchFaceUtil;
import com.mobica.openday.util.GooglePlayServicesUtil;
import com.mobica.openday.util.Logger;

import java.util.concurrent.TimeUnit;

public class FaceService extends CanvasWatchFaceService {

	private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

	@Override
	public MobicaFaceEngine onCreateEngine() {
		return new MobicaFaceEngine();
	}

	private class MobicaFaceEngine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
			GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

		private final String TAG = MobicaFaceEngine.class.getSimpleName();

		private final String MOBICA_LABEL = "Mobica";

		static final int MSG_UPDATE_TIME = 0;

		Paint handPaint;
		Paint labelPaint;
		Time mTime;

		FaceData.ClockType clockType = FaceData.ClockType.ANALOG;
		int backgroundColor;
		int backgroundAmbientColor;
		int labelColor;
		int handsColor;

		boolean isAmbient;
		boolean isLowBitAmbient;
		private float[] labelTextSize;

		/** Handler to update the time once a second in interactive mode. */
		final Handler updateTimeHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
					case MSG_UPDATE_TIME:
						invalidate();

						if (shouldTimerBeRunning()) {
							long timeMs = System.currentTimeMillis();
							long delayMs = INTERACTIVE_UPDATE_RATE_MS
									- (timeMs % INTERACTIVE_UPDATE_RATE_MS);
							updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
						}

						break;
				}
			}
		};

		GoogleApiClient googleApiClient = GooglePlayServicesUtil.
				createWearClientWithListeners(FaceService.this, MobicaFaceEngine.this, MobicaFaceEngine.this);

		@Override
		public void onCreate(SurfaceHolder holder) {
			super.onCreate(holder);
			Logger.l(TAG + ", onCreate");

			setWatchFaceStyle(new WatchFaceStyle.Builder(FaceService.this)
					.setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
					.setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
					.setShowSystemUiTime(false)
					.build());

			preparePaints();

			labelTextSize = measureText(labelPaint, MOBICA_LABEL);

			mTime = new Time();
		}

		@Override
		public void onAmbientModeChanged(boolean inAmbientMode) {
			Logger.l(TAG + ", onAmbientModeChanged");

			super.onAmbientModeChanged(inAmbientMode);

			if (isAmbient != inAmbientMode) {
				isAmbient = inAmbientMode;

				if (isLowBitAmbient) {
					handPaint.setAntiAlias(!inAmbientMode);
				}

				invalidate();
			}

			// Whether the timer should be running depends on whether we're visible (as well as
			// whether we're in ambient mode), so we may need to start or stop the timer.
			updateTimer();
		}

		@Override
		public void onDraw(Canvas canvas, Rect bounds) {
			Logger.l(TAG + ", onDraw");

			handPaint.setColor(handsColor);
			labelPaint.setColor(labelColor);

			mTime.setToNow();

			// Draw background in proper mode.
			canvas.drawColor(isAmbient ? backgroundAmbientColor : backgroundColor);

			switch (clockType) {
				case DIGITAL:
					drawDigitalFace(canvas, bounds);
					break;
				case ANALOG:
					drawAnalogFace(canvas, bounds);
					break;
			}
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			Logger.l(TAG + ", onVisibilityChanged");

			super.onVisibilityChanged(visible);

			if (visible) {
				googleApiClient.connect();
			} else if (googleApiClient != null && googleApiClient.isConnected()) {
				Wearable.DataApi.removeListener(googleApiClient, this);
				googleApiClient.disconnect();
			}

			// Whether the timer should be running depends on whether we're visible (as well as
			// whether we're in ambient mode), so we may need to start or stop the timer.
			updateTimer();
		}

		@Override
		public void onDestroy() {
			Logger.l(TAG + ", onDestroy");
			updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
			super.onDestroy();
		}

		@Override
		public void onPropertiesChanged(Bundle properties) {
			Logger.l(TAG + ", onPropertiesChanged");

			super.onPropertiesChanged(properties);
			isLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
		}

		@Override
		public void onTimeTick() {
			Logger.l(TAG + ", onTimeTick");

			super.onTimeTick();
			invalidate();
		}

		@Override public void onConnected(Bundle bundle) {
			Logger.l(TAG + ", onConnected");

			Wearable.DataApi.addListener(googleApiClient, MobicaFaceEngine.this);
			updateConfigDataItemAndUiOnStartup();
		}

		@Override public void onDataChanged(DataEventBuffer dataEvents) {
			Logger.l(TAG + ", onDataChanged");

			try {
				for (DataEvent dataEvent : dataEvents) {
					if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
						continue;
					}

					DataItem dataItem = dataEvent.getDataItem();
					if (!dataItem.getUri().getPath().equals(
							DigitalWatchFaceUtil.PATH_WITH_FEATURE)) {
						continue;
					}

					DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
					updateUiForConfig(FaceData.fromDataMap(dataMapItem.getDataMap()));
				}
			} finally {
				dataEvents.close();
			}
		}

		private void drawAnalogFace(Canvas canvas, Rect bounds) {
			// Find the center. Ignore the window insets so that, on round watches with a
			// "chin", the watch face is centered on the entire screen, not just the usable
			// portion.
			float centerX = bounds.width() / 2f;
			float centerY = bounds.height() / 2f;

			// Draw MOBICA label.
			canvas.drawText(MOBICA_LABEL, centerX - labelTextSize[0] / 2,
					centerY + labelTextSize[1] / 2, labelPaint);

			// Draw time hands.
			float secRot = mTime.second / 30f * (float) Math.PI;
			int minutes = mTime.minute;
			float minRot = minutes / 30f * (float) Math.PI;
			float hrRot = ((mTime.hour + (minutes / 60f)) / 6f) * (float) Math.PI;

			float secLength = centerX - 20;
			float minLength = centerX - 40;
			float hrLength = centerX - 80;

			if (!isAmbient) {
				float secX = (float) Math.sin(secRot) * secLength;
				float secY = (float) -Math.cos(secRot) * secLength;
				canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, handPaint);
			}

			float minX = (float) Math.sin(minRot) * minLength;
			float minY = (float) -Math.cos(minRot) * minLength;
			canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, handPaint);

			float hrX = (float) Math.sin(hrRot) * hrLength;
			float hrY = (float) -Math.cos(hrRot) * hrLength;
			canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, handPaint);
		}

		private void drawDigitalFace(Canvas canvas, Rect bounds) {
			// TODO
		}

		private void updateTimer() {
			updateTimeHandler.removeMessages(MSG_UPDATE_TIME);

			if (shouldTimerBeRunning()) {
				updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
			}
		}

		private boolean shouldTimerBeRunning() {
			return isVisible() && !isInAmbientMode();
		}

		private void updateConfigDataItemAndUiOnStartup() {
			Logger.l(TAG + ", updateConfigDataItemAndUiOnStartup");

			DigitalWatchFaceUtil.fetchConfigDataMap(googleApiClient,
					new DigitalWatchFaceUtil.FetchConfigDataMapCallback() {
						@Override
						public void onConfigDataMapFetched(FaceData faceData) {
							updateUiForConfig(faceData);
						}
					}
			);
		}

		private void updateUiForConfig(FaceData faceData) {
			Logger.l(TAG + ", updateUiForConfig: " + faceData);

			clockType = faceData.getClockType();

			backgroundColor = faceData.getBackgroundColor();
			backgroundAmbientColor = faceData.getBackgroundAmbientColor();
			labelColor = faceData.getLabelColor();
			handsColor = faceData.getTimeHandsColor();

			invalidate();
		}

		private final Rect sTextBoundsHelper = new Rect();

		private float[] measureText(Paint paint, String text) {
			paint.getTextBounds(text, 0, text.length(), sTextBoundsHelper);
			float textWidth = paint.measureText(text);
			int textHeight = sTextBoundsHelper.height();

			return new float[]{textWidth, textHeight};
		}

		private void preparePaints() {
			Resources resources = FaceService.this.getResources();

			handPaint = new Paint();
			handPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
			handPaint.setAntiAlias(true);
			handPaint.setStrokeCap(Paint.Cap.ROUND);

			labelPaint = new Paint();
			labelPaint.setAntiAlias(true);
			labelPaint.setSubpixelText(true);
			labelPaint.setTextSize(55);
		}

		@Override public void onConnectionSuspended(int i) {
			// Nothing.
		}

		@Override public void onConnectionFailed(ConnectionResult connectionResult) {

		}
	}
}