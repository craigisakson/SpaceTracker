package com.runninghusky.spacetracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class FlightLogService extends Service {
	private NotificationManager mNM;
	private DataHelper dh;
	public Flight f = new Flight();
	public long flightId, smsInterval, lastSent;
	public Context ctx = this;
	public LocationManager fullLocManager;
	public LocationListener fullLocListener;
	public FlightData fd = new FlightData();
	public Integer i = 0;
	public Location oldLoc;
	public float distance;
	private SharedPreferences prefs;
	private Boolean isMetric = false;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = 1;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		FlightLogService getService() {
			return FlightLogService.this;
		}
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		flightId = intent.getLongExtra("flightId", 0);
		this.dh = new DataHelper(this);
		f = this.dh.selectFlightHistoryById(flightId);
		this.dh.close();

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		isMetric = (prefs.getString("unit", "english").equals("english")) ? false
				: true;

		smsInterval = Long.valueOf(f.getSmsDuration()) * 1000;

		fullLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		fullLocListener = new FullLocationListener();
		fullLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, fullLocListener);

		Log.d("end", "return START_STICKY");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		if (fullLocManager != null) {
			fullLocManager.removeUpdates(fullLocListener);
			fullLocManager = null;
		}

		// Tell the user we stopped.
		Toast.makeText(this, "Logging has Stopped", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		CharSequence text = "Logging to Space Tracker...";
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, StopServiceActivity.class), 0);
		notification.setLatestEventInfo(this, text, "Click to stop logging...",
				contentIntent);

		mNM.notify(NOTIFICATION, notification);
	}

	public void sendSMS(String pn, String m) {
		final String phoneNumber = pn;
		final String message = m;
		String SENT = "SMS_SENT";

		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
				SENT), 0);

		// ---when the SMS has been sent---
		registerReceiver(mBroadcastReceiver, new IntentFilter(SENT));

		Calendar c = Calendar.getInstance();
		dh = new DataHelper(ctx);
		dh.insertSms(flightId, phoneNumber, message, c.getTimeInMillis(),
				"sending sms");
		dh.close();

		SmsManager sms = SmsManager.getDefault();
		HlprUtil.toast("Sending sms message...", ctx, true);
		sms.sendTextMessage(phoneNumber, "", message, sentPI, null);

	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String msg = "";
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				msg = "SMS sent";
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				msg = "Generic failure";
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				msg = "No service";
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				msg = "Null PDU";
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				msg = "Radio off";
				break;
			}
		}
	};

	public class FullLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			try {
				if (i == 0) {
					lastSent = System.currentTimeMillis();
					oldLoc = loc;
					dh = new DataHelper(ctx);
					distance = dh.getLastDistance(flightId);
					dh.close();
					i++;
				}

				distance += loc.distanceTo(oldLoc);

				if (i == 0) {
					lastSent = System.currentTimeMillis();
					i++;
				}
				dh = new DataHelper(ctx);
				dh.insertDetails(flightId, (float) loc.getLongitude(),
						(float) loc.getLatitude(), (float) loc.getAltitude(),
						loc.getSpeed(), loc.getTime(), loc.getAccuracy(), loc
								.getBearing(), loc.getProvider(), distance);
				dh.close();

			} catch (Exception e) {
				HlprUtil.toast("Error saving data... " + String.valueOf(e),
						ctx, true);
			}

			oldLoc = loc;
			if (f.getSendSms()
					&& (smsInterval < (System.currentTimeMillis() - lastSent))) {
				try {

					lastSent = System.currentTimeMillis();

					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat(
							"HH:mm:ss MM/dd/yyyy");
					String strCal = sdf.format(date);

					String altitude = "";
					String dist = "";
					String speed = "";
					if (isMetric) {
						altitude = String.valueOf(HlprUtil.roundTwoDecimals(loc
								.getAltitude()))
								+ " meters";
						dist = String.valueOf(HlprUtil
								.roundTwoDecimals(distance * 0.001))
								+ " km";
						speed = String.valueOf(HlprUtil.roundTwoDecimals(loc
								.getSpeed() * 3.6))
								+ "kph ";
					} else {
						altitude = String.valueOf(HlprUtil.roundTwoDecimals(loc
								.getAltitude() * 3.2808399))
								+ " feet";
						dist = String.valueOf(HlprUtil
								.roundTwoDecimals(distance * 0.000621371192))
								+ " miles";
						speed = String.valueOf(HlprUtil.roundTwoDecimals(loc
								.getSpeed() * 2.23693629))
								+ "mph ";
					}

					sendSMS(f.getSmsNumber(true),
							"http://maps.google.com/maps?q="
									+ loc.getLatitude() + ","
									+ loc.getLongitude() + "  Altitude: "
									+ altitude + ", Traveled: " + dist
									+ ", Current Speed: " + speed + " at "
									+ strCal);
				} catch (Exception e) {
					HlprUtil.toast("SMS error... " + String.valueOf(e), ctx,
							true);
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			HlprUtil.toast("GPS Disabled...", ctx, true);
		}

		@Override
		public void onProviderEnabled(String provider) {
			HlprUtil.toast("GPS Enabled...", ctx, true);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
}
