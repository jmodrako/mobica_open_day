package com.mobica.openday.android.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import com.mobica.mobilewearcommons.data.FaceData;
import com.mobica.openday.R;

/**
 * Mobica Open Day 2015
 *
 * @author jacek.modrakowski@mobica.com
 */
public class MobicaFaceConfigurationActivity extends Activity implements RadioGroup.OnCheckedChangeListener,
		ResultCallback<DataApi.DataItemResult> {

	private static final String TAG = MobicaFaceConfigurationActivity.class.getSimpleName();

	private static final int COLOR_DENSITY = 12;

	private static final int COLOR_BACKGROUND = 0;
	private static final int COLOR_TIME_HANDS = 1;
	private static final int COLOR_LABEL = 2;
	private static final int COLOR_BACKGROUND_AMBIENT = 3;

	private GoogleApiClient googleApiClient;

	private FaceData faceData;

	private View background, backgroundAmbient, timeHands, label;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_configuration);

		background = findViewById(R.id.color_background);
		backgroundAmbient = findViewById(R.id.color_ambient_background);
		timeHands = findViewById(R.id.color_time_hands);
		label = findViewById(R.id.color_label);

		RadioGroup group = (RadioGroup) findViewById(R.id.clockTypeRadioGroup);
		group.setOnCheckedChangeListener(this);

		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle connectionHint) {
						Log.d(TAG, "onConnected: " + connectionHint);
						// Now you can use the Data Layer API
					}

					@Override
					public void onConnectionSuspended(int cause) {
						Log.d(TAG, "onConnectionSuspended: " + cause);
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult result) {
						Log.d(TAG, "onConnectionFailed: " + result);
					}
				})
				.addApi(Wearable.API)
				.build();

		faceData = FaceData.createAnalogData();

		handleColorClicks(COLOR_BACKGROUND, faceData.getBackgroundColor());
		handleColorClicks(COLOR_BACKGROUND_AMBIENT, faceData.getBackgroundAmbientColor());
		handleColorClicks(COLOR_LABEL, faceData.getLabelColor());
		handleColorClicks(COLOR_TIME_HANDS, faceData.getTimeHandsColor());
	}

	@Override protected void onResume() {
		super.onResume();
		googleApiClient.connect();
	}

	@Override protected void onStop() {
		super.onStop();
		googleApiClient.disconnect();
	}

	public void onBackgroundColorClick(View view) {
		showColorPicker(COLOR_BACKGROUND);
	}

	public void onAmbientBackgroundColorClick(View view) {
		showColorPicker(COLOR_BACKGROUND_AMBIENT);
	}

	public void onTimeHandsColorClick(View view) {
		showColorPicker(COLOR_TIME_HANDS);
	}

	public void onLabelColorClick(View view) {
		showColorPicker(COLOR_LABEL);
	}

	public void onSyncConfigurationClick(View view) {
		refreshConfiguration();
	}

	private void showColorPicker(final int colorType) {
		ColorPickerDialogBuilder
				.with(this)
				.setTitle(getString(R.string.choose_color))
				.initialColor(Color.WHITE)
				.wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
				.density(COLOR_DENSITY)
				.setOnColorSelectedListener(null)
				.setPositiveButton(getString(android.R.string.ok), new ColorPickerClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int selectedColor, Integer[] integers) {
						handleColorClicks(colorType, selectedColor);
					}
				})
				.setNegativeButton(getString(android.R.string.cancel), null)
				.build()
				.show();
	}

	@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.clockTypeAnalogButton) {
			faceData.setClockType(FaceData.ClockType.ANALOG);
		} else if (checkedId == R.id.clockTypeDigitalButton) {
			faceData.setClockType(FaceData.ClockType.DIGITAL);
		}
	}

	private void handleColorClicks(int colorType, int clickedColor) {
		View view = null;

		switch (colorType) {
			case COLOR_BACKGROUND:
				faceData.setBackgroundColor(clickedColor);
				view = background;
				break;
			case COLOR_BACKGROUND_AMBIENT:
				faceData.setBackgroundAmbientColor(clickedColor);
				view = backgroundAmbient;
				break;
			case COLOR_TIME_HANDS:
				faceData.setTimeHandsColor(clickedColor);
				view = timeHands;
				break;
			case COLOR_LABEL:
				faceData.setLabelColor(clickedColor);
				view = label;
				break;
		}

		if (view != null) {
			view.setBackgroundColor(clickedColor);
		}
	}

	private void refreshConfiguration() {
		PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/mobica_face_conf/clock");
		putDataMapReq.getDataMap().putAll(DataMap.fromBundle(faceData.toBundle()));
		PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
		PendingResult<DataApi.DataItemResult> pendingResult =
				Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

		pendingResult.setResultCallback(this);
	}

	@Override public void onResult(DataApi.DataItemResult dataItemResult) {
		Toast.makeText(this, "onResult: " + dataItemResult.getDataItem(), Toast.LENGTH_SHORT).show();
	}
}
