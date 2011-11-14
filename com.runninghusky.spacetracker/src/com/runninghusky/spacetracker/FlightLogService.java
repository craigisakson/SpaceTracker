package com.runninghusky.spacetracker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
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
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
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
				HlprUtil.toast(msg, ctx, true);
				Calendar c = Calendar.getInstance();
				dh = new DataHelper(ctx);
				dh.insertSms(flightId, phoneNumber, message,
						c.getTimeInMillis(), msg);
				dh.close();

			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String msg = "";
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					msg = "SMS delivered";
					break;
				case Activity.RESULT_CANCELED:
					msg = "SMS not delivered";
					break;
				}
				HlprUtil.toast(msg, ctx, true);
				Calendar c = Calendar.getInstance();
				dh = new DataHelper(ctx);
				dh.insertSms(flightId, phoneNumber, message,
						c.getTimeInMillis(), msg);
				dh.close();
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, "", message, sentPI, deliveredPI);

	}

	public class FullLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			if (i == 0) {
				lastSent = System.currentTimeMillis();
				oldLoc = loc;
				distance = 0;
				i++;
			} else {
				dh = new DataHelper(ctx);
				distance = dh.getLastDistance(flightId);
				dh.close();
			}

			distance += loc.distanceTo(oldLoc);

			dh = new DataHelper(ctx);
			dh.insertDetails(flightId, (float) loc.getLongitude(),
					(float) loc.getLatitude(), (float) loc.getAltitude(),
					loc.getSpeed(), loc.getTime(), loc.getAccuracy(),
					loc.getBearing(), loc.getProvider(), distance);
			dh.close();

			if (f.getSendSms()
					&& (smsInterval < (System.currentTimeMillis() - lastSent))) {

				Calendar cal = new GregorianCalendar();
				cal.setTimeInMillis(loc.getTime());
				String strCal = cal.get(Calendar.HOUR_OF_DAY) + ":"
						+ cal.get(Calendar.MINUTE) + " ";
				strCal += (cal.get(Calendar.AM_PM) == 1) ? "PM" : "AM";
				strCal += " " + cal.get(Calendar.MONTH) + "/"
						+ cal.get(Calendar.DAY_OF_MONTH) + "/"
						+ cal.get(Calendar.YEAR);
				String smsMessage = "http://maps.google.com/maps?q="
						+ loc.getLatitude()
						+ ","
						+ loc.getLongitude()
						+ "  Traveled: "
						+ String.valueOf(HlprUtil
								.roundTwoDecimals(distance * 0.000621371192))
						+ " miles Current Speed: "
						+ String.valueOf(HlprUtil.roundTwoDecimals(loc
								.getSpeed() * 2.23693629)) + "mph at " + strCal;

				Log.d("sms number", f.getSmsNumber(true));
				sendSMS(f.getSmsNumber(true), smsMessage);
				lastSent = System.currentTimeMillis();
			}
			oldLoc = loc;

		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
}
