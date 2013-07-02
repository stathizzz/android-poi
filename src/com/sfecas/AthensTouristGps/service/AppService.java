package com.sfecas.AthensTouristGps.service;
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
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

//import com.aripuca.tracker.NotificationActivity;
//import com.sfecas.oss1.R;

//import com.aripuca.tracker.track.ScheduledTrackRecorder;
//import com.aripuca.tracker.track.TrackRecorder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sfecas.AthensTouristGps.App;
import com.sfecas.AthensTouristGps.BeepManager;
import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.NotificationActivity;
import com.sfecas.AthensTouristGps.SecondMainActivity;
import com.sfecas.AthensTouristGps.ThirdMainActivity;
import com.sfecas.AthensTouristGps.R;

/**
 * this service handles gps (and compass?) updates
 */
public class AppService extends Service
{

	private static boolean running = false;

	private App app;

	private LocationManager locationManager;

	private SensorManager sensorManager;

	Sensor accelerometer, magnetometer;

	private AlarmManager beepManager;
	/**
	 * current device location
	 */
	private Location currentLocation;

	/**
	 * is GPS in use?
	 */
	private boolean gpsInUse;

	/**
	 * is GPS in use?
	 */
	private boolean networkInUse;
	
	/**
	 * listening for location updates flag
	 */
	private boolean listening;

	/**
	 * listening getter
	 */
	public boolean isListening()
	{
		return listening;
	}

	/**
	 * gpsInUse setter
	 */
	public void setGpsInUse(boolean gpsInUse)
	{
		this.gpsInUse = gpsInUse;
	}

	public boolean isGpsInUse()
	{
		return this.gpsInUse;
	}

	/**
	 * gpsInUse setter
	 */
	public void setNetworkInUse(boolean networkInUse)
	{
		this.networkInUse = networkInUse;
	}

	public boolean isNetworkInUse()
	{
		return this.networkInUse;
	}
	
	public boolean isOnline() 
	{
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) 
	    {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Defines a listener that responds to location updates
	 */
	private LocationListener locationListener = new LocationListener()
	{

		// Called when a new location is found by the network location provider.
		@Override
		public void onLocationChanged(Location location)
		{

			listening = true;

			currentLocation = location;
			app.setCurrentLocation(location);

			broadcastLocationUpdate(location, Constants.GPS_PROVIDER, Constants.ACTION_LOCATION_UPDATES);
		}

		/**
		 * Called when the provider status changes. This method is called when a
		 * provider is unable to fetch a location or if the provider has
		 * recently become available after a period of unavailability.
		 */
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
			{
				listening = false;
			}
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}

	};

	/**
	 * Broadcasting location update
	 */
	private void broadcastLocationUpdate(Location location, int locationProvider, String action)
	{

		// let's broadcast location data to any activity waiting for updates
		Intent intent = new Intent(action);

		Bundle bundle = new Bundle();
		bundle.putInt("location_provider", locationProvider);
		bundle.putParcelable("location", location);

		intent.putExtras(bundle);

		sendBroadcast(intent);

	}

	private SensorEventListener compassListener = new SensorEventListener()
	{
		float[] mGravity = null;
		float[] mGeomagnetic = null;
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{

		}

		@Override
		public void onSensorChanged(SensorEvent event)
		{

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				mGravity = event.values;
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			{
				mGeomagnetic = event.values;
			}
			if (mGravity != null && mGeomagnetic != null)
			{
				float R[] = new float[9];
				float I[] = new float[9];
				boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
				if (success)
				{
					float orientation[] = new float[3];
					SensorManager.getOrientation(R, orientation);

					if (app != null)
						// orientation contains: azimuth, pitch and roll
						app.setCurrentAzimuth(orientation[0]*Constants.rad2deg); 

					// let's broadcast compass data to any activity waiting for
					// updates
					Intent intent = new Intent(Constants.ACTION_COMPASS_UPDATES);

					// packing azimuth value into bundle
					Bundle bundle = new Bundle();
					bundle.putFloat("azimuth", orientation[0]*Constants.rad2deg);
					//bundle.putFloat("pitch", orientation[1]*Constants.rad2deg);
					//bundle.putFloat("roll", orientation[2]*Constants.rad2deg);

					intent.putExtras(bundle);

					// broadcasting compass updates
					sendBroadcast(intent);
				}
			}

		}

	};

	// //////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This is the object that receives interactions from clients
	 */
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent)
	{

		Log.d(Constants.TAG, "AppService: BOUND " + this.toString());

		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{

		Log.d(Constants.TAG, "AppService: UNBOUND " + this.toString());

		return true;
	}

	public class LocalBinder extends Binder
	{
		public AppService getService()
		{
			return AppService.this;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Initialize service
	 */
	@Override
	public void onCreate()
	{

		super.onCreate();

		Log.i(Constants.TAG, "AppService: onCreate");

		// registerReceiver(beepRequestReceiver, new
		// IntentFilter(Constants.ACTION_ALERT_FREQUENCY_UPDATES));

		this.app = (App) getApplication();

		// location sensor
		// first time we call startLocationUpdates from MainActivity
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// compass orientation sensor
		this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		this.magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		this.beepManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		AppService.running = true;

		this.requestLastKnownLocation();

		this.startSensorUpdates();
	}

	/**
	 * Service destructor
	 */
	@Override
	public void onDestroy()
	{

		Log.i(Constants.TAG, "AppService: onDestroy");

		// unregisterReceiver(beepRequestReceiver);

		AppService.running = false;

		// stop listener without delay
		this.locationManager.removeUpdates(locationListener);

		this.stopSensorUpdates();

		this.locationManager = null;
		this.sensorManager = null;
		this.beepManager = null;
		super.onDestroy();

	}

	/**
	 * is service running?
	 */
	public static boolean isRunning()
	{
		return running;
	}

	/**
	 * Requesting last location from GPS or Network provider
	 */
	private void requestLastKnownLocation()
	{
		Location location;
		int locationProvider;

		// get last known location from gps provider
		location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location != null)
		{
			locationProvider = Constants.GPS_PROVIDER_LAST;
		}
		else
		{
			// let's try network provider
			location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (location != null)
		{
			locationProvider = Constants.NETWORK_PROVIDER_LAST;
			broadcastLocationUpdate(location, locationProvider, Constants.ACTION_LOCATION_UPDATES);
		}

		currentLocation = location;
		this.app.setCurrentLocation(location);
	}

	/**
	 * start location updates
	 */
	public void startLocationUpdates()
	{
		this.listening = false;

		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

		// setting gpsInUse to true, but listening is still false at this point
		// listening is set to true with first location update in
		// LocationListener.onLocationChanged
		gpsInUse = true;
	}

	/**
	 * Stopping location updates with delay, leaving a chance for new activity
	 * not to restart location listener
	 */
	public void stopLocationUpdates()
	{
		gpsInUse = false;

		(new stopLocationUpdatesThread()).start();
	}

	/**
	 * 
	 */
	public void stopLocationUpdatesNow()
	{

		locationManager.removeUpdates(locationListener);

		listening = false;

		gpsInUse = false;

	}

	private PendingIntent nextBeepRequestSender;

	public void scheduleBeepRequest(int interval)
	{
		app.logd("AppService.scheduleBeepRequest interval:" + interval);

		Intent intent = new Intent(Constants.ACTION_ALERT_FREQUENCY_UPDATES);
		nextBeepRequestSender = PendingIntent.getBroadcast(AppService.this, 0, intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, interval);

		// schedule single alarm
		// AlarmManager alarmManager = (AlarmManager)
		// getSystemService(ALARM_SERVICE);
		beepManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), nextBeepRequestSender);
	}

	/**
	 * start beep listener
	 */
	public void startBeepUpdates()
	{
		this.beepManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 1000, nextBeepRequestSender);
	}

	PendingIntent beepRequestSender;

	/**
	 * stop beep listener
	 */
	public void stopBeepUpdates()
	{
		this.beepManager.cancel(beepRequestSender);
	}

	/**
	 * start compass listener
	 */
	public void startSensorUpdates()
	{
		// deprecated
		//this.sensorManager.registerListener(compassListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);

		// the right way to do it
		sensorManager.registerListener(compassListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(compassListener, magnetometer, SensorManager.SENSOR_DELAY_UI);

	}

	/**
	 * stop compass listener
	 */
	public void stopSensorUpdates()
	{
		this.sensorManager.unregisterListener(compassListener);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Show ongoing notification
	 */
	private void showOngoingNotification()
	{

		int icon = R.drawable.ic_launcher;

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, getString(R.string.notification), when);

		// show notification under ongoing title
		notification.flags += Notification.FLAG_ONGOING_EVENT;

		CharSequence contentTitle = getString(R.string.main_app_title);
		CharSequence contentText = "Test";

		Intent notificationIntent = new Intent(this, NotificationActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(app, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(0, notification);
	}

	private void clearNotifications()
	{
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// remove all notifications
		mNotificationManager.cancelAll();
	}

	/**
	 * stop location update with delay giving us a chance not to restart
	 * listener if other activity requires GPS sensor too. the new activity has
	 * to bind to AppService and set gpsInUse to true
	 */
	private class stopLocationUpdatesThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				// wait for other activities to grab location updates
				sleep(2500);
			}
			catch (Exception e)
			{
			}

			// if no activities require location updates - stop them and save
			// battery
			if (gpsInUse == false)
			{
				locationManager.removeUpdates(locationListener);
				listening = false;
			}

		}
	}

}
