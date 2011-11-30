package com.runninghusky.spacetracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EulaActivity extends Activity {
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eula);
		Button mButtonYes = (Button) findViewById(R.id.ButtonYes);
		Button mButtonNo = (Button) findViewById(R.id.ButtonNo);

		mButtonYes.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				agreed();
			}
		});

		mButtonNo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

	}

	private void agreed() {
		SharedPreferences sp = getSharedPreferences("spacetracker", 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("agreedToEula", true);
		editor.commit();

		Intent myIntent = new Intent(EulaActivity.this,
				SpaceTrackerActivity.class);
		EulaActivity.this.startActivity(myIntent);
		finish();
	}
}
