package com.runninghusky.spacetracker;

import java.text.DecimalFormat;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class HlprUtil {
	public static void toast(String msg, Context ctx, Boolean isShort) {
		if (isShort) {
			Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
		}
	}

	public static String getOnlyNumerics(String str) {
		if (str == null) {
			return null;
		}

		StringBuffer strBuff = new StringBuffer();
		char c;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			if (Character.isDigit(c)) {
				strBuff.append(c);
			}
		}
		return strBuff.toString();
	}

	public static String convertSecondsToTime(double time) {
		int seconds = (int) (time % 60);
		int minutes = (int) ((time / 60) % 60);
		int hours = (int) ((time / 3600) % 24);
		String secondsStr = (seconds < 10 ? "0" : "") + seconds;
		String minutesStr = (minutes < 10 ? "0" : "") + minutes;
		String hoursStr = (hours < 10 ? "0" : "") + hours;
		return new String(hoursStr + ":" + minutesStr + ":" + secondsStr);
	}

	public static long convertTimeToSeconds(String time) {
		try {
			Log.d("convertTime", "time = " + time);
			String[] s = time.split(":");
			return (Long.valueOf(s[0]) * 3600) + (Long.valueOf(s[1]) * 60)
					+ Long.valueOf(s[2]);
		} catch (Exception e) {
			Log.d("convert exc", String.valueOf(e));
			return 180;
		}
	}

	public static double roundTwoDecimals(double d) {
		try {
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			return Double.valueOf(twoDForm.format(d));
		} catch (NumberFormatException nfe) {
			Log.d("nfe", nfe.toString());
			try {
				int i = (int) (d * 100);
				d = (i / 100);
				return d;
			} catch (NumberFormatException ne) {
				return d;
			}
		}
	}
}
