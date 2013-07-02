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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sfecas.AthensTouristGps.helper.*;
import com.sfecas.AthensTouristGps.service.*;

import com.sfecas.AthensTouristGps.R;

import android.app.Activity;

import android.content.BroadcastReceiver;


import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;



import android.location.Location;
import android.os.Bundle;

import android.text.format.DateFormat;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * @author Sfecas Efstathios
 * second activity
 */
public class SecondMainActivity extends Activity
{

	/**
	 * Reference to app object
	 */
	private App app;

	/**
	 * Reference to current location
	 */
	private Location currentLocation;

	private String distanceUnit;
	private String speedUnit;
	private String elevationUnit;
	
	/**
	 * Service connection object
	 */
	private AppServiceConnection serviceConnection;

	/**
	 * Location updates broadcast receiver
	 */
	protected BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{

			Bundle bundle = intent.getExtras();

			currentLocation = (Location) bundle.getParcelable("location");

			updateActivity();
		}
	};

	/**
	 * Called when the activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.azimuth_to_bear);
		app = (App) getApplication();

		serviceConnection = new AppServiceConnection(this, appServiceConnectionCallback);

		// start GPS service only if not started
		startAppService();

		/* Uncomment following for testing purposes */
		/*
		 * TestDefaultOrientation("0"); TestDefaultOrientation("30");
		 * TestDefaultOrientation("60"); TestDefaultOrientation("90");
		 */
	}

	private Runnable appServiceConnectionCallback = new Runnable()
	{

		@Override
		public void run()
		{

			AppService appService = serviceConnection.getService();

			if (appService == null)
			{
				Toast.makeText(SecondMainActivity.this, R.string.gps_service_not_connected, Toast.LENGTH_SHORT).show();
				return;
			}

			// this activity is started by SecondMainActivity which is always
			// listening for location updates
			appService.startLocationUpdates();

			Location location = app.getCurrentLocation();

			// new location was received by the service when activity was paused
			if (location != null)
			{

				currentLocation = location;

				updateActivity();
			}

			// by setting gpsInUse to true we insure that listening will not
			// stop in AppService.stopLocationUpdatesThread
			appService.setGpsInUse(true);
		}
	};

	/**
	 * onResume event handler
	 */
	@Override
	protected void onResume()
	{

		super.onResume();

		//set locale to greek to avoid mantling with geo point commas
		Utils.setLocale(this, "en");

		this.initializeMeasuringUnits();

		// registering receiver for location updates
		registerReceiver(locationBroadcastReceiver, new IntentFilter(Constants.ACTION_LOCATION_UPDATES));

		// bind to AppService
		// appServiceConnectionCallback will be called once bound
		serviceConnection.bindAppService();

	}

	/**
	 * Execute this when activity is partially hidden
	 */
	@Override
	public void onPause()
	{

		unregisterReceiver(locationBroadcastReceiver);

		// unbind AppService
		serviceConnection.unbindAppService();

		if (this.isFinishing())
		{

			// if activity is not going to be recreated - stop service
			stopAppService();

		}
		super.onPause();
	}

	/**
	 * Execute this when activity is hidden
	 */
	@Override
	protected void onDestroy()
	{
		serviceConnection = null;
		app = null;
		super.onDestroy();
	}

	/**
	 * onCreateOptionsMenu handler
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}


	/**
	 * Process main activity menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

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
	private void initializeMeasuringUnits()
	{
		speedUnit = app.getPreferences().getString("speed_units", "km/h");
		distanceUnit = app.getPreferences().getString("distance_units", "km");
		elevationUnit = app.getPreferences().getString("elevation_units", "m");
	}


	/**
	 * Update main activity view
	 */
	public void updateActivity()
	{

		AppService appService = serviceConnection.getService();

		if (currentLocation == null || appService == null)
		{
			return;
		}

		float[] resu;
		try
		{
			resu = GetDistance(currentLocation);
			String distanceStr = Utils.formatDistance(resu[0], distanceUnit);
			((TextView) findViewById(R.id.distance_value)).setText(distanceStr);

			double az = GetAzimuth(resu[1]);
			DecimalFormat df = new DecimalFormat("###.######");
			final TextView res = (TextView) findViewById(R.id.bearing_azimuth_value);
			res.setText(df.format(az));
		}
		catch (Exception e)
		{
			((TextView) findViewById(R.id.distance_value)).setText(getString(R.string.invalid_data));
			((TextView) findViewById(R.id.bearing_azimuth_value)).setText(getString(R.string.invalid_data));
		}

		// update coordinates
		if (findViewById(R.id.lat) != null)
		{
			((TextView) findViewById(R.id.lat)).setText(Utils.formatLat(currentLocation.getLatitude()));
		}
		if (findViewById(R.id.lng) != null)
		{
			((TextView) findViewById(R.id.lng)).setText(Utils.formatLng(currentLocation.getLongitude()));
		}

		// update accuracy data
		if (currentLocation.hasAccuracy())
		{

			float accuracy = currentLocation.getAccuracy();

			if (findViewById(R.id.accuracy) != null)
			{
				((TextView) findViewById(R.id.accuracy)).setText(Utils.PLUSMINUS_CHAR
						+ Utils.formatDistance(accuracy, distanceUnit));
			}
			if (findViewById(R.id.accuracyUnit) != null)
			{
				((TextView) findViewById(R.id.accuracyUnit)).setText(Utils.getLocalizedDistanceUnit(this, accuracy, distanceUnit));
			}
		}

		// last fix time
		if (findViewById(R.id.lastFix) != null)
		{
			String lastFix = (String) DateFormat.format("k:mm:ss", currentLocation.getTime());
			((TextView) findViewById(R.id.lastFix)).setText(lastFix);
		}

		// update elevation data
		if (currentLocation.hasAltitude())
		{
			if (findViewById(R.id.elevation) != null)
			{
				((TextView) findViewById(R.id.elevation)).setText(Utils.formatElevation((float) currentLocation.getAltitude(), elevationUnit));
			}
			if (findViewById(R.id.elevationUnit) != null)
			{
				((TextView) findViewById(R.id.elevationUnit)).setText(Utils.getLocalizedElevationUnit(this, elevationUnit));
			}
		}

		// current speed and pace
		float speed = currentLocation.getSpeed();

		// current speed (cycling, driving)
		if (findViewById(R.id.speed) != null)
		{
			((TextView) findViewById(R.id.speed)).setText(Utils.formatSpeed(speed, speedUnit));
		}

		if (findViewById(R.id.speedUnit) != null)
		{
			((TextView) findViewById(R.id.speedUnit)).setText(Utils.getLocalizedSpeedUnit(this, speedUnit));
		}

		// current pace (running, hiking, walking)
		if (findViewById(R.id.pace) != null)
		{
			((TextView) findViewById(R.id.pace)).setText(Utils.formatPace(speed, speedUnit));
		}
	}

	/**
	 * start GPS listener service
	 */
	private void startAppService()
	{
		// starting GPS listener service
		serviceConnection.startService();
	}

	/**
	 * stop GPS listener service
	 */
	private void stopAppService()
	{
		serviceConnection.stopService();
	}

	/**
	 * get distance between two geo points
	 */
	public float[] GetDistance(Location location) throws Exception
	{
		double lat_f = (double) location.getLatitude();
		double lng_f = (double) location.getLongitude();
		String wp_lat_from = Location.convert(lat_f, Location.FORMAT_DEGREES);
		String wp_lng_from = Location.convert(lng_f, Location.FORMAT_DEGREES);

		final EditText wp_lat_to = (EditText) findViewById(R.id.LatTo2);
		final EditText wp_lng_to = (EditText) findViewById(R.id.LngTo2);
		double lat_to = (double) ((Double.parseDouble(wp_lat_to.getText().toString())));
		double lng_to = (double) ((Double.parseDouble(wp_lng_to.getText().toString())));

		double lat_from = (double) ((Double.parseDouble(wp_lat_from.toString())));
		double lng_from = (double) ((Double.parseDouble(wp_lng_from.toString())));

		float[] results = new float[3];
		Location.distanceBetween(lat_from, lng_from, lat_to, lng_to, results);

		return results;
	}

	/**
	 * get the angle between bearing at current location and input
	 */
	private double GetAzimuth(double intial_bearing)
	{
		/*
		 * we have problems evaluating GetAzimuthDifference, so we use the
		 * bearing from the resu structure
		 */
		final EditText azimuth = (EditText) findViewById(R.id.OrientationAzimuth);
		if (azimuth.getText().toString() == "")
			azimuth.setText("0");
		double az = intial_bearing
				- Double.parseDouble(azimuth.getText().toString());
		if (az < 0)
			az += 360;

		return az;
	}

	/**
	 * not used - get the angle between current location-POI and input
	 */
	private float GetAzimuthDifference(Location me)
	{
		final EditText latPOI = (EditText) findViewById(R.id.LatTo2);
		final EditText lngPOI = (EditText) findViewById(R.id.LngTo2);

		if (latPOI.getText().toString() == "")
			latPOI.setText(getString(R.string.default_orientation_lat));
		if (lngPOI.getText().toString() == "")
			lngPOI.setText(getString(R.string.default_orientation_lng));

		double lat = Double.parseDouble(latPOI.getText().toString()) * 1E6;
		double lng = Double.parseDouble(lngPOI.getText().toString()) * 1E6;

		Location POI = new Location("");
		POI.setLatitude(lat);
		POI.setLongitude(lng);
		final EditText azimuth = (EditText) findViewById(R.id.OrientationAzimuth);
		if (azimuth.getText().toString() == "")
			azimuth.setText("0");

		float res = (float) me.bearingTo(POI)
				- Float.parseFloat(azimuth.getText().toString());
		if (res < 0)
			res += 360;

		return res;
	}

}
