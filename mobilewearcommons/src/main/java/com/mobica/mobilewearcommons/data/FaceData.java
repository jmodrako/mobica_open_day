package com.mobica.mobilewearcommons.data;

import android.graphics.Color;
import android.os.Bundle;
import com.google.android.gms.wearable.DataMap;

/**
 * Mobica Open Day 2015
 *
 * @author jacek.modrakowski@mobica.com
 */
public class FaceData {

	private static final String KEY_CLOCK_TYPE = "clock_type";
	private static final String KEY_COLOR_LABEL = "color_label";
	private static final String KEY_COLOR_BCKG = "color_background";
	private static final String KEY_COLOR_AMBIENT_BCKG = "color_ambient_background";
	private static final String KEY_COLOR_TIME_HDS = "color_time_heads";
	private static final String KEY_TIMESTAMP = "timestamp";

	public enum ClockType {
		DIGITAL, ANALOG;
	}

	private int clockType;

	private int backgroundAmbientColor;
	private int backgroundColor;
	private int timeHandsColor;
	private int labelColor;

	public static FaceData createAnalogData() {
		FaceData result = new FaceData();

		result.setClockType(ClockType.ANALOG);
		result.setBackgroundColor(Color.GRAY);
		result.setLabelColor(Color.WHITE);
		result.setTimeHandsColor(Color.RED);
		result.setBackgroundAmbientColor(Color.BLACK);

		return result;
	}

	public static FaceData fromDataMap(DataMap dataMap) {
		FaceData result = new FaceData();

		result.setClockType(ClockType.valueOf(dataMap.getString(KEY_CLOCK_TYPE)));
		result.setBackgroundColor(dataMap.getInt(KEY_COLOR_BCKG));
		result.setLabelColor(dataMap.getInt(KEY_COLOR_LABEL));
		result.setTimeHandsColor(dataMap.getInt(KEY_COLOR_TIME_HDS));
		result.setBackgroundAmbientColor(dataMap.getInt(KEY_COLOR_AMBIENT_BCKG));

		return result;
	}

	public int getBackgroundAmbientColor() {
		return backgroundAmbientColor;
	}

	public void setBackgroundAmbientColor(int backgroundAmbientColor) {
		this.backgroundAmbientColor = backgroundAmbientColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setTimeHandsColor(int timeHandsColor) {
		this.timeHandsColor = timeHandsColor;
	}

	public int getTimeHandsColor() {
		return timeHandsColor;
	}

	public ClockType getClockType() {
		return ClockType.values()[clockType];
	}

	public void setClockType(ClockType clockType) {
		this.clockType = clockType.ordinal();
	}

	public int getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;
	}

	public Bundle toBundle() {
		Bundle result = new Bundle();

		result.putInt(KEY_COLOR_BCKG, backgroundColor);
		result.putInt(KEY_COLOR_LABEL, labelColor);
		result.putInt(KEY_COLOR_TIME_HDS, timeHandsColor);
		result.putInt(KEY_COLOR_AMBIENT_BCKG, backgroundAmbientColor);
		result.putInt(KEY_CLOCK_TYPE, clockType);
		result.putLong(KEY_TIMESTAMP, System.currentTimeMillis());

		return result;
	}

	private FaceData() {
		// Prevents object creation.
	}
}
