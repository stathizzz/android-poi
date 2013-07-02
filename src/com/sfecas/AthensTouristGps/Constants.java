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
public abstract class Constants {

	public static final String TAG = "OSS2";
	
	//public static final String DB_NAME= "PoiDb";
	
	public static final String APP_NAME = "oss2";
	
	/**
	 * POIs table
	 */
	public static final String POI_TABLE = "poi";
	
	/**
	 * map view modes
	 */
	public static final int MAP_STREET = 0;
	public static final int MAP_SATELLITE = 1;
	
	public static final float MIN_SPEED = 0.224F; // meters per second

	public static final int SEGMENT_NONE = 0;
	public static final int SEGMENT_PAUSE_RESUME = 1;
	public static final int SEGMENT_DISTANCE = 2;
	public static final int SEGMENT_TIME = 3;
	public static final int SEGMENT_CUSTOM_1 = 4;
	public static final int SEGMENT_CUSTOM_2 = 5;
	
	public static final int ORIENTATION_PORTRAIT = 1;
	public static final int ORIENTATION_LANDSCAPE = 2;
	public static final int ORIENTATION_REVERSE_LANDSCAPE = 10;
	public static final int ORIENTATION_REVERSE_PORTRAIT = 20;
	
	/**
	 * location providers
	 */
	public static final int GPS_PROVIDER = 0;
	public static final int GPS_PROVIDER_LAST = 1;
	public static final int NETWORK_PROVIDER = 2;
	public static final int NETWORK_PROVIDER_LAST = 3;
	
	/**
	 * map modes
	 */
	public static final int SHOW_POI = 0;
	public static final int SHOW_UNKNOWN = 1;
	
	
	public static final float rad2deg = 180/(float) Math.PI;  
	/**
	 * intent actions
	 */
	public static final String ACTION_ALERT_FREQUENCY_UPDATES = "com.sfecas.oss1.ACTION_ALERT_FREQUENCY_UPDATES";
	public static final String ACTION_NEXT_LOCATION_REQUEST = "com.sfecas.oss1.ACTION_NEXT_LOCATION_REQUEST";
	public static final String ACTION_NEXT_TIME_LIMIT_CHECK = "com.sfecas.oss1.ACTION_NEXT_TIME_LIMIT_CHECK";
	public static final String ACTION_START_SENSOR_UPDATES = "com.sfecas.oss1.ACTION_START_SENSOR_UPDATES";
	public static final String ACTION_COMPASS_UPDATES = "com.sfecas.oss1.ACTION_COMPASS_UPDATES";
	public static final String ACTION_LOCATION_UPDATES = "com.sfecas.oss1.ACTION_LOCATION_UPDATES";
	public static final String ACTION_NETWORK_UPDATES = "com.sfecas.oss1.ACTION_NETWORK_UPDATES";
	
	/* vibrate or beep? */
	public static final String KEY_PLAY_BEEP = "search_sound";
	public static final String KEY_VIBRATE = "search_vibration";
	  
	public static final String ACTION_SCHEDULED_LOCATION_UPDATES = "com.sfecas.oss1.ACTION_SCHEDULED_LOCATION_UPDATES";

}