package com.runninghusky.spacetracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FlightHistoryActivity extends Activity {
	private long flightId;
	private ListView lvl;
	private List<FlightData> flightData = new ArrayList<FlightData>();
	private DataHelper dh;
	private SimpleAdapter mFlight;
	private TextView tvTitle;

	protected static final int CONTEXT_MAP_IT = 1;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.histflight);

		Bundle b = getIntent().getExtras();

		flightId = b.getLong("flightId", 0);

		tvTitle = (TextView) findViewById(R.id.TextViewHistTitle);
		tvTitle.setText(b.getString("title"));
//		Log.d("title", b.getString("title"));
		
		
		
		this.dh = new DataHelper(this);
		flightData = this.dh.selectFlightData(flightId);
		this.dh.close();

		lvl = (ListView) findViewById(R.id.ListViewFlightData);

		if (!flightData.isEmpty()) {
			ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map = new HashMap<String, String>();
			for (FlightData f : flightData) {
				map.put("id", Long.toString(f.getId()));
				map.put("longitude", "Longitude:  "
						+ String.valueOf(f.getLongitude()));
				map.put("latitude", "Latitude:  "
						+ String.valueOf(f.getLatitude()));
				map.put("elevation", "Elevation:  "
						+ String.valueOf(f.getElevation()));
				map.put("speed", "Speed:  " + String.valueOf(f.getSpeed()));
				map.put("time", "Time:  " + String.valueOf(f.getTime()));
				mylist.add(map);
				map = new HashMap<String, String>();
			}

			mFlight = new SimpleAdapter(this, mylist, R.layout.histrow,
					new String[] { "id", "longitude", "latitude", "elevation",
							"speed", "time" }, new int[] { R.id.id,
							R.id.TextViewLongitude, R.id.TextViewLatitude,
							R.id.TextViewElevation, R.id.TextViewSpeed,
							R.id.TextViewTime });
			lvl.setAdapter(mFlight);

			/* Add Context-Menu listener to the ListView. */
			lvl
					.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
						// @Override
						public void onCreateContextMenu(ContextMenu menu,
								View v, ContextMenu.ContextMenuInfo menuInfo) {

							menu.add(0, CONTEXT_MAP_IT, 0, "Map IT!!");
						}
					});

			// lvl.setOnItemClickListener(new OnItemClickListener() {
			// public void onItemClick(AdapterView<?> a, View v, int position,
			// long id) {
			// Object o = lvl.getItemAtPosition(position);
			// HashMap fullObject = (HashMap) o;
			// flightId = Long.parseLong(fullObject.get("id").toString());
			// initHistory();
			// }
			// });
		} else {
			lvl.setVisibility(View.INVISIBLE);
			HlprUtil.toast("No flight history exists...", this, false);
		}

	}

	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		Object o = lvl.getItemAtPosition(menuInfo.position);
		HashMap fullObject = (HashMap) o;
		flightId = Long.parseLong(fullObject.get("id").toString());
		switch (item.getItemId()) {
		case CONTEXT_MAP_IT:
			// DeleteSingleSchedule();
			break;
		// case CONTEXT_EDIT:
		// sendSetupIntent(flightId, false);
		// break;
		// case CONTEXT_OPEN:
		// sendHistoryIntent(flightId);
		// break;
		// case CONTEXT_DELETE:
		// deleteSingleFlight(flightId);
		// break;
		// case CONTEXT_EXPORT_KML:
		// getSomeDataForEmail();
		// sendEmailKML();
		// break;
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	public void goBackIntent() {
		Intent myIntent = new Intent(FlightHistoryActivity.this,
				SpaceTrackerActivity.class);
		FlightHistoryActivity.this.startActivity(myIntent);
		finish();
	}

}
