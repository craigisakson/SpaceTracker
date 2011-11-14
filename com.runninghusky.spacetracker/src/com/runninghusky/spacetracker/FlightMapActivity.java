package com.runninghusky.spacetracker;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class FlightMapActivity extends MapActivity {
	private List<Overlay> mapOverLays;
	private Projection projection;
	MapView mapView;
	MapController mapC;
	GeoPoint p;
	String locationName;
	private List<FlightData> flightData = new ArrayList<FlightData>();
	private DataHelper dh;
	private long flightId;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goBackIntent();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		Bundle b = getIntent().getExtras();

		flightId = b.getLong("flightId", 0);

		this.dh = new DataHelper(this);
		flightData = this.dh.selectFlightData(flightId);
		this.dh.close();

		mapView = (MapView) findViewById(R.id.maps);
		mapView.setBuiltInZoomControls(true);

		mapOverLays = mapView.getOverlays();
		projection = mapView.getProjection();
		if (!flightData.isEmpty()) {
			mapOverLays.add(new MyOverlay());
			mapC = mapView.getController();
			for (FlightData f : flightData) {
				p = new GeoPoint((int) (f.getLatitude() * 1E6), (int) (f
						.getLongitude() * 1E6));
				break;
			}
			try {
				mapC.animateTo(p);
				mapC.setZoom(17);
			} catch (Exception e) {
				Log.d("exc", String.valueOf(e));
			}
		} else {
			HlprUtil.toast("No flight history exists...", this, false);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// Menu Creation
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 2, 0, "ChangeView");
		return true;
	}

	// When meniItem Clikced
	public boolean onOptionsItemSelected(MenuItem item) {
		final MapController mc = mapView.getController();
		switch (item.getItemId()) {
		case 2:
			AlertDialog dialog = new AlertDialog.Builder(FlightMapActivity.this)
					.setItems(R.array.selectMapView,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (which == 0) {
										mapView.setSatellite(true);
									} else if (which == 1) {
										mapView.setSatellite(false);
									}
								}

							}).create();
			dialog.setTitle("Select MapView");
			dialog.show();
			break;
		}
		return true;
	}

	class MapsOverlay extends com.google.android.maps.Overlay {

		public boolean draw(Canvas canvas, MapView mapview, boolean shadow,
				long when) {
			super.draw(canvas, mapview, shadow);
			Point screenPts = new Point();
			// converting geoPoint to screen co-ordinates
			mapview.getProjection().toPixels(p, screenPts);

			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.bubble);
			canvas.drawBitmap(bmp, screenPts.x - 12, screenPts.y - 34, null);
			return true;
		}
	}

	class MyOverlay extends Overlay {
		// private Projection proj;
		public MyOverlay() {
		}

		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
			if (!shadow) {
				super.draw(canvas, mapv, shadow);
			}

			Paint mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(4);
			Path path = new Path();

			for (int i = 0; i < flightData.size() - 1; i++) {
				GeoPoint gP1 = new GeoPoint((int) (flightData.get(i)
						.getLatitude() * 1E6), (int) (flightData.get(i)
						.getLongitude() * 1E6));
				GeoPoint gP2 = new GeoPoint((int) (flightData.get(i + 1)
						.getLatitude() * 1E6), (int) (flightData.get(i + 1)
						.getLongitude() * 1E6));

				Point p1 = new Point();
				Point p2 = new Point();
				path = new Path();

				projection.toPixels(gP1, p1);
				projection.toPixels(gP2, p2);

				path.moveTo(p2.x, p2.y);
				path.lineTo(p1.x, p1.y);
				canvas.drawPath(path, mPaint);
			}
		}
	}

	public void goBackIntent() {
		Intent myIntent = new Intent(FlightMapActivity.this,
				SpaceTrackerActivity.class);
		FlightMapActivity.this.startActivity(myIntent);
		finish();
	}
}
