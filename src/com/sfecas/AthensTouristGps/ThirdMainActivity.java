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
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.google.gson.Gson;
import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.PoiListActivity.PoiArrayAdapter;
import com.sfecas.AthensTouristGps.helper.*;
import com.sfecas.AthensTouristGps.service.*;
import com.sfecas.AthensTouristGps.view.CompassImage;
import com.sfecas.AthensTouristGps.webservice.*;
import com.sfecas.AthensTouristGps.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * third activity
 */
public class ThirdMainActivity extends Activity
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
	private int coordUnit;
	private boolean signalUnit;
	// true or magnetic north?
	boolean trueNorth;

	private long declinationLastUpdate = 0;

	/**
	 * On the current scenario the gps navigation is bound to a POI, so the id
	 * of the selected by the user POI will give all the required data from the
	 * database.
	 */
	
	private Location currentPoi;
	private PoiArrayAdapter adapter;
	
	private long recordId = -1;

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

			if (bundle != null)
				currentLocation = (Location) bundle.getParcelable("location");

			updateActivity();
		}
	};

	/**
	 * compass updates broadcast receiver
	 */
	protected BroadcastReceiver compassBroadcastReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{

			if (app == null) {
				return;
			}

			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				float az = bundle.getFloat("azimuth");
				az = az>0? az: 360+az;
				updateCompass(az);
				app.setCurrentAzimuth(az);
			}
		}
	};

	/**
	 * POIs data (location) broadcast receiver
	 */
	protected BroadcastReceiver beepBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();
			if (bundle != null)
			{
				updateBeep(bundle.getInt("seconds"));
			}
		}
	};

	/**
	 * Called when the activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directions); // directions
		app = (App) getApplication();

		serviceConnection = new AppServiceConnection(this, appServiceConnectionCallback);

		// start GPS service only if not started
		startAppService();

	}

	private Runnable appServiceConnectionCallback = new Runnable()
	{
		@Override
		public void run()
		{
			AppService appService = serviceConnection.getService();

			if (appService == null)
			{
				Toast.makeText(ThirdMainActivity.this, R.string.gps_service_not_connected, Toast.LENGTH_SHORT).show();
				return;
			}

			// this activity is started by SecondMainActivity which is always
			// listening for location updates
			appService.startLocationUpdates();

			//appService.startSensorUpdates();
			
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

		// set locale to greek to avoid mantling with geo point commas
		Utils.setLocale(this, "en");

		this.initializeMeasuringUnits();
			
		// registering receiver for location updates
		registerReceiver(locationBroadcastReceiver, new IntentFilter(Constants.ACTION_LOCATION_UPDATES));

			// registering receiver for compass updates
		registerReceiver(compassBroadcastReceiver, new IntentFilter(Constants.ACTION_COMPASS_UPDATES));

		// registering receiver for compass updates
		registerReceiver(beepBroadcastReceiver, new IntentFilter(Constants.ACTION_ALERT_FREQUENCY_UPDATES));

		// bind to AppService
		// appServiceConnectionCallback will be called once bound
		serviceConnection.bindAppService();

		// instantiate a beep manager.
		beep = new BeepManager(this.app);

		// currentPoiId = intent.getLongExtra("currentPoiId", -1);
		POI p = app.getCurrentPOI();
		if (p != null)
		{
			/* the the selected poi on the main screen */
			showPOI(p);
			app.setCurrentPOI(p);
			/* update gps data on main screen */
			updateActivity();
		}

	}

	/**
	 * Execute this when activity is partially hidden
	 */
	@Override
	public void onPause()
	{

		Log.i(Constants.TAG, "ThirdMainActivity: onPause");
		unregisterReceiver(locationBroadcastReceiver);
		unregisterReceiver(compassBroadcastReceiver);
		unregisterReceiver(beepBroadcastReceiver);

		AppService appService = serviceConnection.getService();

		if (appService != null)
		{
			if (!this.isFinishing())
			{

				// stop location updates when not recording track
				appService.stopLocationUpdates();
				//appService.stopSensorUpdates();
			}
		}

		// unbind AppService
		serviceConnection.unbindAppService();

		if (this.isFinishing())
		{

			// if activity is not going to be recreated - stop service
			stopAppService();

		}

		if (adapter != null)
			adapter.clear();

		if (beep != null)
			beep.release();

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

	private void showPOI(POI poi)
	{
		String distStr = "";
		String bearingStr = "";

		float newAzimuth = 0;
		float newBearing = 0;

		if (poi != null)
		{
			if (currentLocation != null)
			{
				// currentLocation.distanceTo(wp.getLocation());
				float[] distanceTo = Utils.GetDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), poi.getLat(), poi.getLng());

				distStr = Utils.formatDistance(distanceTo[0], distanceUnit)
						+ " "
						+ Utils.getLocalizedDistanceUnit(ThirdMainActivity.this, distanceTo[0], distanceUnit);

				poi.setDistanceTo(distanceTo[0]);

				newBearing = currentLocation.bearingTo(poi.getLocation());

				if ((int) newBearing < 0)
				{
					newBearing = 360 - Math.abs((int) newBearing);
				}

				newAzimuth = newBearing
						- app.getCurrentAzimuth()
						- (float) Utils.getDeviceRotation(ThirdMainActivity.this);
				if ((int) newAzimuth < 0)
				{
					newAzimuth = 360 - Math.abs((int) newAzimuth);
				}

				bearingStr = Utils.formatNumber(newBearing, 0)
						+ Utils.DEGREE_CHAR;

			}

			ImageView image = (ImageView) findViewById(R.id.imgOnMain);
			Drawable dr = Utils.arrayToDrawable(this, poi.getImage());
			image.setImageDrawable(dr);

			TextView poiTitle = (TextView) findViewById(R.id.titleOnMain);
			TextView poiDetails = (TextView) findViewById(R.id.detailsOnMain);

			// Set value for the first text field
			if (poiTitle != null)
			{
				poiTitle.setText(Utils.shortenStr(poi.getTitle().toString(), 32));
			}

			// set value for the second text field
			if (poiDetails != null)
			{
				poiDetails.setText(Utils.formatLat(poi.getLocation().getLatitude(), coordUnit)
						+ "|"
						+ Utils.formatLng(poi.getLocation().getLongitude(), coordUnit)
						+ "|"
						+ Utils.formatNumber(poi.getLocation().getAltitude(), 0)
						+ ""
						+ Utils.getLocalizedElevationUnit(ThirdMainActivity.this, elevationUnit)
						+ "|" + bearingStr);
			}

		}
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
		POI currentpoi;
		POI nextpoi;
		
		int itemId = item.getItemId();
		if (itemId == R.id.settingsMenuItem) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} else if (itemId == R.id.firstMenuItem) {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
			return true;
		} else if (itemId == R.id.secondMenuItem) {
			startActivity(new Intent(this, PoiListActivity.class));
			return true;
		} else if (itemId == R.id.thirdMenuItem) {
			if (currentLocation == null)
			{
				Toast.makeText(ThirdMainActivity.this, R.string.gps_service_not_connected, Toast.LENGTH_SHORT).show();
				return true;
			}
			POI poi = findNearestPOI(currentLocation);
			if (poi != null)
			{
				showPOI(poi);
				app.setCurrentPOI(poi);
				updateActivity();
			}
			else
				Toast.makeText(ThirdMainActivity.this, R.string.no_nearest_poi, Toast.LENGTH_SHORT).show();
			return true;
		} else if (itemId == R.id.fourthMenuItem) {
			currentpoi = app.getCurrentPOI();
			if (currentpoi == null)
			{
				Toast.makeText(ThirdMainActivity.this, R.string.no_current_poi, Toast.LENGTH_SHORT).show();
				return true;
			}
			nextpoi = findNextNearestPOI(currentpoi);
			if (nextpoi != null)
			{
				showPOI(nextpoi);
				app.setCurrentPOI(nextpoi);
				updateActivity();
			}
			else
				Toast.makeText(ThirdMainActivity.this, R.string.no_next_poi, Toast.LENGTH_SHORT).show();
			return true;
		} else if (itemId == R.id.fifthMenuItem) {
			if (currentLocation == null)
			{
				Toast.makeText(ThirdMainActivity.this, R.string.gps_service_not_connected, Toast.LENGTH_SHORT).show();
				return true;
			}
			/* See if internet is on and utilize the wikiLocation api */
			AppService appService = serviceConnection.getService();
			if (!appService.isOnline())
			{
				Toast.makeText(ThirdMainActivity.this, R.string.internet_off, Toast.LENGTH_LONG).show();
				return true;
			}
			startActivity(new Intent(this, ArticleListActivity.class));
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
		signalUnit = Boolean.parseBoolean(app.getPreferences().getString("signal_method_units", "true"));
		coordUnit = Integer.parseInt(app.getPreferences().getString("coord_units", "0"));
		trueNorth = app.getPreferences().getBoolean("true_north", true);
	}

	/**
	 * Update main activity view
	 */
	public void updateActivity()
	{
		try
		{
			AppService appService = serviceConnection.getService();

			if (currentLocation == null || appService == null)
			{
				return;
			}
			POI p = app.getCurrentPOI();

			if (p != null)
			{

				float[] resu = GetDistanceToPOI(currentLocation, p.getLat(), p.getLng());

				if (resu == null)
				{
					Toast.makeText(ThirdMainActivity.this, R.string.no_current_poi, Toast.LENGTH_SHORT).show();
					return;
				}

				sendBeepIntervalResult(resu);

				String distanceStr = Utils.formatDistance(resu[0], distanceUnit);
				((TextView) findViewById(R.id.distance_value)).setText(distanceStr);
				
				String dir = GetDirections(app.getCurrentAzimuth());
				((TextView) findViewById(R.id.command)).setText(dir);
			}
			
			DecimalFormat df = new DecimalFormat("###.######");
			((TextView) findViewById(R.id.bearing_azimuth_value)).setText(df.format((double) app.getCurrentAzimuth()));

		}
		catch (Exception e)
		{
			((TextView) findViewById(R.id.distance_value)).setText(getString(R.string.invalid_data));
			((TextView) findViewById(R.id.bearing_azimuth_value)).setText(getString(R.string.invalid_data));
			((TextView) findViewById(R.id.command)).setText(getString(R.string.invalid_data));
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

	Timer timer = new Timer();
	final Handler handler = new Handler();
	TimerTask doAsynchronousTask;
	BeepManager beep;

	private void updateBeep(int seconds)
	{
		if (seconds == 0)
			return;

		if (doAsynchronousTask != null)
		{

			doAsynchronousTask.cancel();
			doAsynchronousTask = null;
		}

		if (doAsynchronousTask == null)
		{
			doAsynchronousTask = new TimerTask()
			{
				@Override
				public void run()
				{
					handler.post(new Runnable()
					{
						public void run()
						{
							try
							{
								if (beep != null)
								{
									beep.playBeepSoundOrVibrate();
								}
							}
							catch (Exception e)
							{
								Log.d(ALARM_SERVICE, "Could not play beep sound or vibrate");
								// TODO Auto-generated catch block
							}
						}
					});
				}
			};
		}

		timer.schedule(doAsynchronousTask, 0, seconds * 1000);
	}

	private POI getPOIfromId(long poiId)
	{
		String sql;
		Cursor tmpCursor;
		sql = "SELECT * FROM poi WHERE _id=" + poiId + ";";
		tmpCursor = app.getDatabase().rawQuery(sql, null);
		tmpCursor.moveToFirst();

		POI p = new POI(tmpCursor.getString(tmpCursor.getColumnIndex("title")), tmpCursor.getString(tmpCursor.getColumnIndex("description")), tmpCursor.getFloat(tmpCursor.getColumnIndex("latitude")), tmpCursor.getFloat(tmpCursor.getColumnIndex("longitude")), tmpCursor.getInt(tmpCursor.getColumnIndex("activity")), tmpCursor.getString(tmpCursor.getColumnIndex("url")), tmpCursor.getBlob(tmpCursor.getColumnIndex("barcode")), tmpCursor.getBlob(tmpCursor.getColumnIndex("image")));

		p.setId(tmpCursor.getLong(tmpCursor.getColumnIndex("_id")));

		tmpCursor.close();

		return p;
	}

	/**
	 * Update compass image and azimuth text
	 */
	public void updateCompass(float azimuth)
	{
		float trueAzimuth = 0;

		float declination = findDeclinationPeriodically();

		// magnetic north to true north
		trueAzimuth = azimuth + declination;
		if (trueAzimuth > 360)
		{
			trueAzimuth -= 360;
		}

		if (findViewById(R.id.azimuth) != null)
		{
			((TextView) findViewById(R.id.azimuth)).setText(Utils.formatNumber(trueAzimuth, 0)
					+ Utils.DEGREE_CHAR
					+ " "
					+ Utils.getDirectionCode(trueAzimuth));
		}

		// update compass image
		if (findViewById(R.id.compassImage) != null)
		{
			CompassImage compassImage = (CompassImage) findViewById(R.id.compassImage);

			compassImage.setAngle(360 - trueAzimuth
					- Utils.getDeviceRotation(this));

			compassImage.invalidate();
		}

	}

	private float findDeclinationPeriodically()
	{
		// let's not request declination on every compass update
		float declination = 0;
		if (trueNorth && currentLocation != null)
		{
			long now = System.currentTimeMillis();
			// let's request declination every 20 minutes, not every compass
			// update
			if (now - declinationLastUpdate > 20 * 60 * 1000)
			{
				declination = Utils.getDeclination(currentLocation, now);
				declinationLastUpdate = now;
			}
		}
		return declination;
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

	private void sendBeepIntervalResult(float[] ditanceResults)
	{
		// let's broadcast compass data to any activity waiting for updates
		Intent intent = new Intent(Constants.ACTION_ALERT_FREQUENCY_UPDATES);

		// packing azimuth value into bundle
		Bundle bundle = new Bundle();

		if (ditanceResults[0] > 1000)
		{
			bundle.putInt("seconds", 10);
		}
		else if (ditanceResults[0] > 500 && ditanceResults[0] <= 1000)
		{
			bundle.putInt("seconds", 7);
		}
		else if (ditanceResults[0] > 100 && ditanceResults[0] <= 500)
		{
			bundle.putInt("seconds", 5);
		}
		else if (ditanceResults[0] > 20 && ditanceResults[0] <= 100)
		{
			bundle.putInt("seconds", 3);
		}
		else if (ditanceResults[0] > 0 && ditanceResults[0] <= 20)
		{
			bundle.putInt("seconds", 1);
		}

		intent.putExtras(bundle);

		// broadcasting compass updates
		sendBroadcast(intent);
	}

	/**
	 * get directions to the destination POI
	 */
	public String GetDirections(double angle)
	{
		if (Math.abs(angle) <= 30.0 || Math.abs(angle) >= 330.0) // +- 30 degrees angle
		{
			return getString(R.string.drive_straight);
		}
		else if (Math.abs(angle) <= 210.0 && Math.abs(angle) >= 150.0)
		{
			return getString(R.string.drive_back);
		}
		else if (Math.abs(angle) > 30.0 && Math.abs(angle) < 150.0)
		{
			return getString(R.string.drive_right);
		}
		else if (Math.abs(angle) > 210 && Math.abs(angle) < 330)
		{
			return getString(R.string.drive_left);
		}
		return getString(R.string.default_text);
	}

	/**
	 * get distance between two geo points
	 */
	public float[] GetDistanceOSS1(Location location) throws Exception
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
	 * get distance between two geo points
	 */
	public float[] GetDistance(Location location, double lat_to, double lng_to) throws Exception
	{
		double lat_f = (double) location.getLatitude();
		double lng_f = (double) location.getLongitude();
		String wp_lat_from = Location.convert(lat_f, Location.FORMAT_DEGREES);
		String wp_lng_from = Location.convert(lng_f, Location.FORMAT_DEGREES);

		double lat_from = (double) ((Double.parseDouble(wp_lat_from.toString())));
		double lng_from = (double) ((Double.parseDouble(wp_lng_from.toString())));

		float[] results = new float[3];
		Location.distanceBetween(lat_from, lng_from, lat_to, lng_to, results);

		return results;
	}

	/**
	 * get distance between two geo points
	 */
	public float[] GetDistanceToPOI(Location location, double lat_to, double lng_to)
	{		
		return Utils.GetDistance(location.getLatitude(), location.getLongitude(), lat_to, lng_to);
	}

	public POI findNearestPOI(Location currentLocation)
	{
		ArrayList<POI> pois = Utils.updatePOIsArray(app);

		BasePoiArrayAdapter adapter = new BasePoiArrayAdapter(this, R.layout.poi_list_item, currentLocation, pois);
		
		adapter.sortByDistance();

		ArrayList<POI> sortedPois = adapter.getItems();

		/* the first element after the sort is the nearest to us */
		if (sortedPois.size() > 0)
			return sortedPois.get(0);

		return null;
	}

	public POI findNextNearestPOI(POI currentPOI)
	{
		ArrayList<POI> pois = Utils.updatePOIsArray(app);
		BasePoiArrayAdapter adapter = new BasePoiArrayAdapter(this, R.layout.poi_list_item, app.getCurrentLocation(), pois);
		
		adapter.sortByDistance();

		ArrayList<POI> sortedPois = adapter.getItems();
		POI wp = null;
		for (POI p : sortedPois)
			if (p.getId() == currentPOI.getId())
			{
				wp = p;
				break;
			}
		if (wp == null)
			return null;

		if (sortedPois.size() > sortedPois.indexOf(wp)+1)
			/*
			 * the first element after the sort is the nearest to the current
			 * POI
			 */
			return sortedPois.get(sortedPois.indexOf(wp) + 1);
		else
			return sortedPois.get(sortedPois.indexOf(wp));
	}
	
	/**
	 * not used - get the angle between current location-POI and input
	 */
	public float GetAzimuthDifference(Location me)
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
		final EditText azimuthTxt = (EditText) findViewById(R.id.OrientationAzimuth);
		if (azimuthTxt.getText().toString() == "")
			azimuthTxt.setText("0");

		float res = (float) me.bearingTo(POI)
				- Float.parseFloat(azimuthTxt.getText().toString());
		if (res < 0)
			res += 360;

		return res;
	}

	
	
	protected class PoiArrayAdapter extends BasePoiArrayAdapter
	{

		Bitmap arrowBitmap;
		BitmapDrawable bmd;

		public PoiArrayAdapter(Context context, int textViewResourceId, Location currentLocation,
				ArrayList<POI> items)
		{

			super(context, textViewResourceId, currentLocation, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			View v = convertView;

			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.poi_list_item, null);
			}

			String distStr = "";
			String bearingStr = "";

			float newAzimuth = 0;
			float newBearing = 0;

			POI wp = this.getItems().get(position);
			if (wp != null)
			{
				if (currentLocation != null)
				{
					float[] distanceTo = Utils.GetDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), wp.getLat(), wp.getLng());

					distStr = Utils.formatDistance(distanceTo[0], distanceUnit)
							+ " "
							+ Utils.getLocalizedDistanceUnit(ThirdMainActivity.this, distanceTo[0], distanceUnit);

					wp.setDistanceTo(distanceTo[0]);

					newBearing = currentLocation.bearingTo(wp.getLocation());

					if ((int) newBearing < 0)
					{
						newBearing = 360 - Math.abs((int) newBearing);
					}

					newAzimuth = newBearing
							- app.getCurrentAzimuth()
							- (float) Utils.getDeviceRotation(ThirdMainActivity.this);
					if ((int) newAzimuth < 0)
					{
						newAzimuth = 360 - Math.abs((int) newAzimuth);
					}

					bearingStr = Utils.formatNumber(newBearing, 0)
							+ Utils.DEGREE_CHAR;

				}

				TextView poiTitle = (TextView) v.findViewById(R.id.poi_title);
				TextView poiDetails = (TextView) v.findViewById(R.id.poi_details);
				TextView poiDistance = (TextView) v.findViewById(R.id.poi_distance);

				// Set value for the first text field
				if (poiTitle != null)
				{
					poiTitle.setText(Utils.shortenStr(wp.getTitle().toString(), 32));
				}

				// set value for the second text field
				if (poiDetails != null)
				{
					poiDetails.setText(Utils.formatLat(wp.getLocation().getLatitude(), coordUnit)
							+ "|"
							+ Utils.formatLng(wp.getLocation().getLongitude(), coordUnit)
							+ "|"
							+ Utils.formatNumber(wp.getLocation().getAltitude(), 0)
							+ ""
							+ Utils.getLocalizedElevationUnit(ThirdMainActivity.this, elevationUnit)
							+ "|" + bearingStr);
				}

				if (poiDistance != null)
				{
					poiDistance.setText(distStr);
				}

				ImageView image = (ImageView) v.findViewById(R.id.list_image);
				Drawable dr = Utils.arrayToDrawable(this.getContext(), wp.getImage());
				image.setImageDrawable(dr);

				// rotating small arrow pointing to POI
				CompassImage im = (CompassImage) v.findViewById(R.id.compassImage);
				im.setAngle(newAzimuth);

			}

			return v;

		}
	}

	private double[] findCoordsRegex(String context)
	{
		try
		{
			Pattern p = Pattern.compile("([-+]?\\d+\\.\\d+).?([-+]?\\d+\\.\\d+)");
			Matcher m = p.matcher(context);
			m.find();

			double[] p3 = { Double.parseDouble(m.group(1)),
					Double.parseDouble(m.group(2)) };
			return p3;
		}
		catch (Exception ex)
		{
			Toast.makeText(ThirdMainActivity.this, R.string.fail_barcode_read, Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == 0)
		{
			if (resultCode == RESULT_OK)
			{
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Identify the POI from its latitude and longitude given on the
				// DB
				double coords[] = findCoordsRegex(contents);

				if (coords != null && coords.length == 2)
				{
					double lat_to = coords[0];
					double lng_to = coords[1];
					POI tmp = Utils.getPOIfromLocation(app, lat_to, lng_to);

					if (tmp == null)
					{
						Toast.makeText(ThirdMainActivity.this, R.string.no_poi_on_db, Toast.LENGTH_LONG).show();
						return;
					}

					app.setCurrentPOI(tmp);
				}
			}
			else if (resultCode == RESULT_CANCELED)
			{
				// Handle cancel
				Toast.makeText(ThirdMainActivity.this, R.string.fail_barcode_read, Toast.LENGTH_SHORT).show();
			}
		}
	}

}
