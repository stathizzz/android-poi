package com.sfecas.AthensTouristGps;
/*
Copyright (c) 2013, Efstathios D. Sfecas  <stathizzz@gmail.com>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.SettingsActivity;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.service.AppService;
import com.sfecas.AthensTouristGps.R;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * Reference to Application object
	 */
	private App app;
	
	private String distanceUnit;

	/**
	 * onCreate event handler
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_coords);

		app = ((App) getApplication());	
		this.setControlButtonListeners();
	}

	/**
	 * onResume event handler
	 */
	@Override
	protected void onResume() {

		super.onResume();
		Log.i(Constants.TAG, "MainActivity: onResume");
		this.initializeMeasuringUnits();
	}
	
	/**
	 * onPause event handler
	 */
	@Override
	protected void onPause() {

		Log.i(Constants.TAG, "MainActivity: onPause");
		super.onPause();
	}
	
	/**
	 * onDestroy event handler
	 */
	@Override
	protected void onDestroy() {

		Log.i(Constants.TAG, "MainActivity: onDestroy");
		app = null;
		super.onDestroy();
	}
	
	/**
	 * onCreateOptionsMenu handler
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Process main activity menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();
		if (itemId == R.id.settingsMenuItem) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} else if (itemId == R.id.firstMenuItem) {
			startActivity(new Intent(this, MyCaptureActivity.class));
			return true;
		} else if (itemId == R.id.secondMenuItem) {
			startActivity(new Intent(this, PoiListActivity.class));
			return true;
		} else if (itemId == R.id.thirdMenuItem) {
			startActivity(new Intent(this, ThirdMainActivity.class));
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * initialize measuring units with up to date values
	 */
	private void initializeMeasuringUnits() {

		distanceUnit = app.getPreferences().getString("distance_units", "km");
	}

	/**
	 * Setting up listeners for application buttons
	 */
	private void setControlButtonListeners() {

		((Button) findViewById(R.id.applyBtn))
				.setOnClickListener(findDistanceListener);
	}

	private OnClickListener findDistanceListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (((EditText) findViewById(R.id.LatFrom)).getText().toString() != ""
					&& ((EditText) findViewById(R.id.LngFrom)).getText().toString() != ""
					&& ((EditText) findViewById(R.id.LatTo)).getText().toString() != ""
					&& ((EditText) findViewById(R.id.LngTo)).getText().toString() != "") {

				// processing is done in separate thread
				distanceWorker(MainActivity.this, new DistanceHandler());

			} else {
				// sth went wrong - invalid data
				Toast.makeText(MainActivity.this, R.string.invalid_data,
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * Geocoder handler class. Gets a message from geocoder thread 
	 */
	private class DistanceHandler extends Handler {
		/**
		 * Processing message from distance thread
		 */
		@Override
		public void handleMessage(Message message) {

			String distanceStr;
			switch (message.what) {
			case 1:
				Bundle bundle = message.getData();
				distanceStr = bundle.getString("distanceHor");
				break;
			default:
				distanceStr = "";
			}
			if (distanceStr != "") {
				TextView t = ((TextView) findViewById(R.id.distanceTxt));
				t.setText(distanceStr);
			} else {
				// sth went wrong - invalid data
				Toast.makeText(MainActivity.this, R.string.invalid_data,
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * Running distance calculator as a separate thread. The thread will send a
	 * message to provided Handler object in order to update UI
	 */
	private void distanceWorker(final Context context, final Handler handler) {

		Thread thread = new Thread() {

			@Override
			public void run() {

				float resu[] = { -1, -1, -1 };

				try {
					resu = FindDistance();

				} catch (Exception e) {

					Log.e(Constants.TAG, "Cannot find distance", e);

				} finally {

					// sending message to a handler
					Message msg = Message.obtain();
					msg.setTarget(handler);

					if (resu.length > 0) {
						msg.what = 1;

						String distanceStr = Utils.formatDistance(resu[0], distanceUnit);
							
						// passing address string in the bundle
						Bundle bundle = new Bundle();
						bundle.putString("distanceHor", distanceStr);

						msg.setData(bundle);

					} else {
						msg.what = 0;
					}
					msg.sendToTarget();
				}
			}
		};
		thread.start();
	}

	/**
	 * get distance between two geo points given as input
	 */
	public float[] FindDistance() {
		final EditText wp_lat_from = (EditText) findViewById(R.id.LatFrom);
		final EditText wp_lng_from = (EditText) findViewById(R.id.LngFrom);
		double lat_from = (double) (Double.parseDouble(wp_lat_from.getText()
				.toString()));
		double lng_from = (double) (Double.parseDouble(wp_lng_from.getText()
				.toString()));

		final EditText wp_lat_to = (EditText) findViewById(R.id.LatTo);
		final EditText wp_lng_to = (EditText) findViewById(R.id.LngTo);
		double lat_to = (double) (Double.parseDouble(wp_lat_to.getText()
				.toString()));
		double lng_to = (double) (Double.parseDouble(wp_lng_to.getText()
				.toString()));

		float[] results = new float[3];
		Location.distanceBetween(lat_from, lng_from, lat_to, lng_to, results);

		return results;
	}

	

	

}
