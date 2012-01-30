package com.runninghusky.spacetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StopServiceActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (myServiceIsRunning()) {
		stopService(new Intent(this, FlightLogService.class));
		// }
		finish();
	}

	// private boolean myServiceIsRunning() {
	// ActivityManager manager = (ActivityManager)
	// getSystemService(ACTIVITY_SERVICE);
	// for (RunningServiceInfo service : manager
	// .getRunningServices(Integer.MAX_VALUE)) {
	// if ("com.runninghusky.rdo.sample.UploadService"
	// .equals(service.service.getClassName())) {
	// return true;
	// }
	// }
	// return false;
	// }

}
