package com.sfecas.AthensTouristGps.helper;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.sfecas.AthensTouristGps.App;
import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.ThirdMainActivity;
import com.sfecas.AthensTouristGps.webservice.WikiArticle;
import com.sfecas.AthensTouristGps.webservice.WikiLocator;
import com.sfecas.AthensTouristGps.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * @author Sfecas Efstathios
 */
public class Utils
{

	public final static long ONE_SECOND = 1000;
	public final static long SECONDS = 60;
	public final static long ONE_MINUTE = ONE_SECOND * 60;
	public final static long MINUTES = 60;
	public final static long ONE_HOUR = ONE_MINUTE * 60;
	public final static long HOURS = 24;
	public final static long ONE_DAY = ONE_HOUR * 24;

	public static final char DEGREE_CHAR = (char) 0x00B0;
	public static final char PLUSMINUS_CHAR = (char) 0x00B1;

	protected final static double KM_TO_MI = 0.621371192;
	protected final static double M_TO_FT = 3.2808399;
	protected final static double MI_TO_M = 1609.344;
	protected final static double MI_TO_FEET = 5280.0;
	protected final static double KMH_TO_MPH = 0.621371192;
	protected final static double KMH_TO_KNOTS = 0.539957;

	/**
	 * set locale
	 */
	public static void setLocale(Activity activity, String locale_name)
	{
		Locale locale = new Locale(locale_name);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		activity.getBaseContext().getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());
	}

	/**
	 * test known POIs around a central one
	 */
	public static void TestDefaultOrientation(String myAzimuth, Context context)
	{
		double lat = Double.parseDouble(context.getString(R.string.default_orientation_lat));
		double lng = Double.parseDouble(context.getString(R.string.default_orientation_lng));
		double lat_north = Double.parseDouble(context.getString(R.string.default_orientation_lat_north));
		double lng_north = Double.parseDouble(context.getString(R.string.default_orientation_lng_north));
		double lat_east = Double.parseDouble(context.getString(R.string.default_orientation_lat_east));
		double lng_east = Double.parseDouble(context.getString(R.string.default_orientation_lng_east));
		double lat_south = Double.parseDouble(context.getString(R.string.default_orientation_lat_south));
		double lng_south = Double.parseDouble(context.getString(R.string.default_orientation_lng_south));
		double lat_west = Double.parseDouble(context.getString(R.string.default_orientation_lat_west));
		double lng_west = Double.parseDouble(context.getString(R.string.default_orientation_lng_west));

		Location POI = new Location("POI");
		POI.setLatitude(lat);
		POI.setLongitude(lng);
		Location POI_north = new Location("POI_north");
		POI_north.setLatitude(lat_north);
		POI_north.setLongitude(lng_north);
		Location POI_east = new Location("POI_east");
		POI_east.setLatitude(lat_east);
		POI_east.setLongitude(lng_east);
		Location POI_south = new Location("POI_south");
		POI_south.setLatitude(lat_south);
		POI_south.setLongitude(lng_south);
		Location POI_west = new Location("POI_west");
		POI_west.setLatitude(lat_west);
		POI_west.setLongitude(lng_west);

		float angle_north = POI.bearingTo(POI_north);
		float angle_east = POI.bearingTo(POI_east);
		float angle_south = POI.bearingTo(POI_south);
		float angle_west = POI.bearingTo(POI_west);

		StringBuilder b = new StringBuilder();
		b.append("angle_north: ").append(String.valueOf(angle_north)).append('\n');
		b.append("angle_east: ").append(String.valueOf(angle_east)).append('\n');
		b.append("angle_south: ").append(String.valueOf(angle_south)).append('\n');
		b.append("angle_west: ").append(String.valueOf(angle_west)).append('\n');
		Toast.makeText(context, b.toString(), Toast.LENGTH_LONG).show();

		return;
	}

	public static String formatNumber(Object value, int max)
	{
		return Utils.formatNumber(value, max, 0);
	}

	/**
	 * Number formatting according to default locale
	 */
	public static String formatNumber(Object value, int max, int min)
	{

		NumberFormat f = NumberFormat.getInstance();
		f.setMaximumFractionDigits(max);
		f.setMinimumFractionDigits(min);
		f.setGroupingUsed(false);

		try
		{
			return f.format(value);
		}
		catch (IllegalArgumentException e)
		{
			return "err";
		}

	}

	/**
	 * Format distance based on unit type
	 * 
	 * @param float value
	 * @return String
	 */
	public static String formatDistance(float value, String unit)
	{

		if (unit.equals("km"))
		{
			if (value > 100000)
			{
				return Utils.formatNumber(value / 1000, 1);
			}

			// convert to km
			if (value > 1000)
			{
				return Utils.formatNumber(value / 1000, 2, 1);
			}

			// leave value in kilometers with 6 precision
			return Utils.formatNumber(value / 1000, 6);

		}

		if (unit.equals("mi"))
		{

			if (value > 100 * MI_TO_M)
			{
				return Utils.formatNumber(value / 1000, 1);
			}

			// convert to miles
			if (value > MI_TO_M)
			{
				return Utils.formatNumber(value / MI_TO_M, 2, 1);
			}

			// value is in feet
			return Utils.formatNumber(value / 1000 * M_TO_FT, 6);
		}

		return "";

	}

	public static String getLocalizedDistanceUnit(Context context, float value, String unit)
	{

		if (unit.equals("km"))
		{

			if (value > 1000)
			{
				return context.getString(R.string.km);
			}
			return context.getString(R.string.m);
		}

		if (unit.equals("mi"))
		{
			if (value > MI_TO_M)
			{
				return context.getString(R.string.mi);
			}
			return context.getString(R.string.ft);
		}

		return "";

	}

	public static String formatElevation(double value, String unit)
	{

		if (unit.equals("m"))
		{
			return Utils.formatNumber(value, 0);
		}

		if (unit.equals("ft"))
		{
			return Utils.formatNumber(value * M_TO_FT, 0);
		}

		return "";
	}

	public static String getLocalizedElevationUnit(Context context, String unit)
	{

		if (unit.equals("m"))
		{
			return context.getString(R.string.m);
		}

		if (unit.equals("ft"))
		{
			return context.getString(R.string.ft);
		}

		return "";
	}

	/**
	 * Format speed value (kph, mph or knots)
	 */
	public static String formatSpeed(float value, String unit)
	{

		if (value < 0.224)
		{
			return "0";
		}

		if (unit.equals("km/h"))
		{
			return Utils.formatNumber(value * 3.6, 1, 1);
		}

		if (unit.equals("mi/h"))
		{
			return Utils.formatNumber(value * 3.6 * KM_TO_MI, 1, 1);
		}

		if (unit.equals("kn"))
		{
			return Utils.formatNumber(value * 3.6 * KMH_TO_KNOTS, 1);
		}

		return "";

	}

	public static String getLocalizedSpeedUnit(Context context, String unit)
	{

		if (unit.equals("km/h"))
		{
			return context.getString(R.string.kph);
		}

		if (unit.equals("mi/h"))
		{
			return context.getString(R.string.mph);
		}

		if (unit.equals("kn"))
		{
			return context.getString(R.string.kn);
		}

		return "";

	}

	/**
	 * @param value
	 *            Speed value is in meters per second
	 * @param unit
	 *            km/h or mph
	 * @return
	 */
	public static String formatPace(float value, String unit)
	{

		if (value < 0.224)
		{
			return "00:00";
		}

		if (unit.equals("kph"))
		{
			return formatInterval((long) (1000000 / value), false);
		}

		if (unit.equals("mph"))
		{
			return formatInterval((long) (1000000 / (value * KMH_TO_MPH)), false);
		}

		if (unit.equals("kn"))
		{
			return formatInterval((long) (1000000 / (value * KMH_TO_KNOTS)), false);
		}

		return "";

	}

	public static String formatInterval(long milliseconds, boolean showHours)
	{

		int seconds = Math.round(milliseconds / 1000.0f);

		int hours = (int) (seconds / 3600);
		int minutes = (int) (seconds / 60);
		if (minutes >= 60)
		{
			minutes = (int) (minutes % 60);
		}
		seconds = (int) (seconds % 60);

		StringBuilder builder = new StringBuilder();

		if (hours > 0 || showHours)
		{
			builder.append(hours);
			builder.append(":");
		}

		if (minutes <= 9)
		{
			builder.append("0");
		}
		builder.append(minutes);

		builder.append(":");

		if (seconds <= 9)
		{
			builder.append("0");
		}
		builder.append(seconds);

		return builder.toString();

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FORMAT COORDINATES
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String formatLat(double lat, int outputType)
	{

		String direction = "N";
		if (lat < 0)
		{
			direction = "S";
			lat = -lat;
		}

		return formatCoord(lat, outputType) + direction;

	}

	public static String formatLat(double lat)
	{
		return formatLat(lat, Location.FORMAT_DEGREES);
	}

	public static String formatLng(double lng, int outputType)
	{

		String direction = "E";
		if (lng < 0)
		{
			direction = "W";
			lng = -lng;
		}

		return formatCoord(lng, outputType) + direction;

	}

	public static String formatLng(double lng)
	{
		return formatLng(lng, Location.FORMAT_DEGREES);
	}

	/**
	 * Formats coordinate value to string based on output type (modified version
	 * from Android API)
	 */
	public static String formatCoord(double coordinate, int outputType)
	{

		StringBuilder sb = new StringBuilder();
		char endChar = DEGREE_CHAR;

		DecimalFormat df = new DecimalFormat("###.######");
		if (outputType == Location.FORMAT_MINUTES
				|| outputType == Location.FORMAT_SECONDS)
		{

			df = new DecimalFormat("##.###");

			int degrees = (int) Math.floor(coordinate);
			sb.append(degrees);
			sb.append(DEGREE_CHAR); // degrees sign
			endChar = '\''; // minutes sign
			coordinate -= degrees;
			coordinate *= 60.0;

			if (outputType == Location.FORMAT_SECONDS)
			{

				df = new DecimalFormat("##.##");

				int minutes = (int) Math.floor(coordinate);
				sb.append(minutes);
				sb.append('\''); // minutes sign
				endChar = '\"'; // seconds sign
				coordinate -= minutes;
				coordinate *= 60.0;
			}
		}

		sb.append(df.format(coordinate));
		sb.append(endChar);

		return sb.toString();
	}

	/**
	 * Simple coordinate decimal formatter
	 * 
	 * @param coord
	 * @return
	 */
	public static String formatCoord(double coord)
	{
		DecimalFormat df = new DecimalFormat("###.######");
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		return df.format(coord);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// COMPASS DECLINATION
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get current magnetic declination
	 * 
	 * @param location
	 * @param timestamp
	 * @return
	 */
	public static float getDeclination(Location location, long timestamp)
	{

		GeomagneticField field = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(), (float) location.getAltitude(), timestamp);

		return field.getDeclination();
	}

	/**
	 * SurfaceView.setZOrderOnTop call through reflection
	 */
	public static void setZOrderOnTop(SurfaceView surfaceView, boolean onTop)
	{

		Method method;
		try
		{

			method = Class.forName("android.view.SurfaceView").getMethod("setZOrderOnTop", boolean.class);

			if (method != null)
			{
				method.invoke(surfaceView, true);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * return device rotation angle
	 */
	@SuppressLint("NewApi")
	public static int getDeviceRotation(Activity activity)
	{

		Display display = activity.getWindowManager().getDefaultDisplay();

		final int rotation = display.getRotation();

		if (rotation == Surface.ROTATION_90)
		{
			return 90;
		}
		else if (rotation == Surface.ROTATION_180)
		{
			return 180;
		}
		else if (rotation == Surface.ROTATION_270)
		{
			return 270;
		}

		return 0;
	}

	public int getDeviceOrientation(Activity activity)
	{

		int orientation;

		Display display = activity.getWindowManager().getDefaultDisplay();

		// determining orientation based on display width and height
		if (display.getWidth() == display.getHeight())
		{
			orientation = Configuration.ORIENTATION_SQUARE;
		}
		else
		{
			if (display.getWidth() < display.getHeight())
			{
				orientation = Configuration.ORIENTATION_PORTRAIT;
			}
			else
			{
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}

		if (orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			return 90;
		}

		return 0;

	}

	public static String getDirectionCode(float azimuth)
	{
		String directionCodes[] = { "N", "NE", "E", "SE", "S", "SW", "W", "NW",
				"N" };
		return directionCodes[Math.round(azimuth / 45)];
	}

	public static String shortenStr(String s, int maxLength)
	{

		if (s.length() > maxLength)
		{
			return s.substring(0, maxLength) + "...";
		}

		return s;
	}

	// Image and Db
	public static byte[] drawableToArray(Context context, int drawableId)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try
		{
			Drawable bmImage = context.getResources().getDrawable(drawableId);
			Bitmap bitmap = ((BitmapDrawable) bmImage).getBitmap();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

			return stream.toByteArray();
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public static Drawable arrayToDrawable(Context context, byte[] array)
	{
		try
		{
			Drawable image = null;
			image = new BitmapDrawable(BitmapFactory.decodeByteArray(array, 0, array.length));

			return image;
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// POIs
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * POIs array
	 */
	public static ArrayList<POI> updatePOIsArray(App app)
	{

		try
		{
			Log.d(Constants.TAG, "updatePOIsArray");

			ArrayList<POI> pois = new ArrayList<POI>();

			Cursor cursor = app.getDatabase().rawQuery("SELECT * FROM poi", null);
			cursor.moveToFirst();

			while (cursor.isAfterLast() == false)
			{

				POI p = new POI(cursor.getString(cursor.getColumnIndex("title")), cursor.getString(cursor.getColumnIndex("description")), cursor.getFloat(cursor.getColumnIndex("latitude")), cursor.getFloat(cursor.getColumnIndex("longitude")), cursor.getInt(cursor.getColumnIndex("activity")), cursor.getString(cursor.getColumnIndex("url")), cursor.getBlob(cursor.getColumnIndex("barcode")), cursor.getBlob(cursor.getColumnIndex("image")));

				p.setId(cursor.getLong(cursor.getColumnIndex("_id")));

				pois.add(p);

				cursor.moveToNext();
			}

			cursor.close();

			return pois;
		}
		catch (Exception ex)
		{
			return null;
		}

	}

	public static POI getPOIfromLocation(App app, double latitude, double longitude)
	{
		POI p = null;
		String sql;
		Cursor tmpCursor;
		latitude = (latitude > 1E6) ? latitude : latitude*1E6;
		longitude = (longitude > 1E6) ? longitude : longitude*1E6;
		sql = "SELECT * FROM poi WHERE latitude=" + latitude*1E6 + " AND longitude=" + longitude*1E6 + ";";
		tmpCursor = app.getDatabase().rawQuery(sql, null);
		boolean isAround = tmpCursor.moveToFirst();
		if (isAround)
		{
			p = new POI(tmpCursor.getString(tmpCursor.getColumnIndex("title")), tmpCursor.getString(tmpCursor.getColumnIndex("description")), tmpCursor.getFloat(tmpCursor.getColumnIndex("latitude")), tmpCursor.getFloat(tmpCursor.getColumnIndex("longitude")), tmpCursor.getInt(tmpCursor.getColumnIndex("activity")), tmpCursor.getString(tmpCursor.getColumnIndex("url")), tmpCursor.getBlob(tmpCursor.getColumnIndex("barcode")), tmpCursor.getBlob(tmpCursor.getColumnIndex("image")));
			p.setId(tmpCursor.getLong(tmpCursor.getColumnIndex("_id")));
		}
		tmpCursor.close();

		return p;
	}
	/**
	 * get distance between two geo points
	 */
	public static float[] GetDistance(double lat_from, double lng_from, double lat_to, double lng_to)
	{
		try
		{
			float[] results = new float[3];
			lat_from = (lat_from > 1E6) ? lat_from / 1E6 : lat_from;
			lng_from = (lng_from > 1E6) ? lng_from / 1E6 : lng_from;
			lat_to = (lat_to > 1E6) ? lat_to / 1E6 : lat_to;
			lng_to = (lng_to > 1E6) ? lng_to / 1E6 : lng_to;
			Location.distanceBetween(lat_from, lng_from, lat_to, lng_to, results);

			return results;
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}
