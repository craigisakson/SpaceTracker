package com.runninghusky.spacetracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;

public class ContainerActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(myServiceIsRunning()){
			stopService(new Intent(this, FlightLogService.class));
		}
		finish();
	}

	private boolean myServiceIsRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.runninghusky.spacetracker.FlightLogService"
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
