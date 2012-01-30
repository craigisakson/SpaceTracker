package com.runninghusky.spacetracker;

import java.util.List;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class Preferences extends PreferenceActivity {
	private Camera mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		mCamera = Camera.open();
		List<Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
		mCamera.release();

		ListPreference cameraSizesList = (ListPreference) findPreference("resolution");
		if (cameraSizesList != null) {
			CharSequence entries[] = new String[sizes.size()];
			CharSequence entryValues[] = new String[sizes.size()];
			int i = 0;
			for (Size s : sizes) {
				entries[i] = String.valueOf(s.width) + " x "
						+ String.valueOf(s.height);
				entryValues[i] = String.valueOf(s.width) + ":"
						+ String.valueOf(s.height);
				i++;
			}
			cameraSizesList.setEntries(entries);
			cameraSizesList.setEntryValues(entryValues);
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do something on back.
			Intent myIntent = new Intent(Preferences.this,
					SpaceTrackerActivity.class);
			Preferences.this.startActivity(myIntent);

			finish();
		}

		return super.onKeyDown(keyCode, event);
	}

}
