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
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.sfecas.AthensTouristGps.helper.AppLog;
import com.sfecas.AthensTouristGps.helper.POI;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.R;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;

import android.os.Environment;
import android.preference.PreferenceManager;

import android.util.Log;
import android.widget.ImageView;
import android.graphics.drawable.BitmapDrawable;

public class App extends Application
{

	/**
	 * Android shared preferences
	 */
	private SharedPreferences preferences;

	/**
	 * application directory
	 */
	private String appDir;

	/**
	 * is external storage writable
	 */
	private boolean externalStorageWriteable = false;

	/**
	 * is external storage available, ex: SD card
	 */
	private boolean externalStorageAvailable = false;

	/**
	 * set a flag for whether the db was created long time before we started up the app
	 */
	private boolean dbPastCreated = false;
	/**
	 * the current location
	 */
	private Location currentLocation;

	/**
	 * the current azimuth readings
	 */
	private float azimuth;
	
	/**
	 * the current POI destination
	 */
	private POI currentPOI;
	/**
	 * database object
	 */
	private SQLiteDatabase db;

	
	@Override
	public void onCreate()
	{
		super.onCreate();

		// accessing preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		setExternalStorageState();

		// database helper
		OpenHelper openHelper = new OpenHelper(this);

		// SQLiteDatabase
		db = openHelper.getWritableDatabase();

		// set application external storage folder
		appDir = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/" + Constants.APP_NAME;

		// create all folders required by the application on external storage
		if (getExternalStorageAvailable() && getExternalStorageWriteable())
		{
			createFolderStructure();
		}

		// adding POIs to db..
		if (dbPastCreated)
			openHelper.insertPoiData();
		
		this.logd("=================== app: onCreate ===================");
	}
	
	public SQLiteDatabase getDatabase()
	{
		return db;
	}

	/**
	 * 
	 */
	public void setDatabase()
	{
		OpenHelper openHelper = new OpenHelper(this);
		db = openHelper.getWritableDatabase();
	}

	/**
	 * application database create/open helper class
	 */
	public class OpenHelper extends SQLiteOpenHelper
	{
		private Context context;
		// private static final String DATABASE_PATH = Constants.DB_NAME ;
		private static final String DATABASE_NAME = Constants.APP_NAME + ".db";

		private static final int DATABASE_VERSION = 1;


		/**
		 * POIs table create sql
		 */
		private static final String POI_TABLE_CREATE = "CREATE TABLE "
				+ Constants.POI_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "title TEXT NOT NULL," + "description TEXT,"
				+ "activity INTEGER," + "latitude NUMERIC,"
				+ "longitude NUMERIC," + "url TEXT," + "barcode BLOB,"
				+ "image BLOB)";

		private static final String POI_TABLE_REMOVE = "DELETE FROM " + Constants.POI_TABLE;

		/**
		 * OpenHelper constructor
		 */
		OpenHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		/**
		 * Creating db for the application
		 */
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try
			{
				//db.execSQL(POI_TABLE_REMOVE);
				db.execSQL("DROP TABLE IF EXISTS " + Constants.POI_TABLE);
				db.execSQL(POI_TABLE_CREATE);
				dbPastCreated = true;
			}
			catch (Exception ex)
			{
				String s = ex.getMessage();
				return;
			}
		}

		/**
		 * Upgrading db for the application
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			try
			{
				// recreate database if version changes (DATABASE_VERSION)
				//db.execSQL(POI_TABLE_REMOVE);
				db.execSQL("DROP TABLE IF EXISTS " + Constants.POI_TABLE);
				onCreate(db);
			}
			catch (Exception ex)
			{
				String s = ex.getMessage();
				return;
			}
		}

		private void insertPoiData()
		{

			// create array of POIs
			ArrayList<POI> poilist = new ArrayList<POI>();
			poilist.add(new POI("������� ���������", "�� M������ ��������� ����� ������������ ������� ������������� ��� �������� ��� ������������� ����� ��� ��������� ��� ������. �� ������� �������� ��� �� �������� ���� ����������� ��� ���� ������ ���� ���� ���� ����� ��� ��������� ��� ����� �������� ��� ����������� ��� ������ ������� ������� ��� ��� ��������� ������� ��� ��� ������� ��� ����������������� ����� ��� ���������� ��������� ���� ���� ������������ ���� �����������, ��������� ��� �������� ��� ������� ���������� ������. �� ��� ������ ��� �������� ����������� �� 2003 ��� ������ ��� �� ����� ���� 21 ������� 2009. ���� 20 ������� ��� 2009, �����������������, ������������, �� �������� ��� �������� ��� ��� ������� ��� ����������� ������ ��������, �������� ��� �������� ��� �.�. ��� ������� ����� ������. � ���� �������� ���������� ������� �������, �� ��� ��������� ������, ��� ���������� �� �������� ��� �����, ���������� ������� �������� ��� ��������� ��� �� ������� ��� ��������, ��� ������ ��� ���������. � ������ ���� ��������� �� �������� ������ ��� ��������� ��� �������� ��� ��� ������� ��� ���������. ���������� ������� 4.000 ����������� �� ��� ���� 14.000 ������������ ������", (float) 37.968578, (float) 23.729132, 0, "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=L&choe=ISO-8859-1&chl=geo%3A37.968578%2C23.729132%3Fq%3DAcropolis+Museum", (byte[]) Utils.drawableToArray(this.context, R.drawable.acropolismuseum_barcode), (byte[]) Utils.drawableToArray(this.context, R.drawable.acropolismuseum)));

			poilist.add(new POI("������ ��������", "�� ������ ��� �������� ����� � �������������� ������� ��������� ��������� ����� ���� ������ �����. ���������� ����� ��� ����� ��� ���������� �������� ��� ��������� ���� �� ������� ��� ��������� ��� ������ � ������� ����� ���������� ��� ������� ��������, ���� ����������� �� ������ ��������, �� ���������� �������� ������ ��� ����� ��� ������. �� ��������� ��������� ��� �������� ��� 5�� ��� ��� 4�� �.�. ��. ��������� -����������� �� ������������- ��� �� �������� �� ����� ��� ����.", (float) 37.970413, (float) 23.727678, 0, "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=L&choe=ISO-8859-1&chl=geo%3A37.970413%2C23.727678%3Fq%3DDionysou+Theatre", (byte[]) Utils.drawableToArray(this.context, R.drawable.dionysoutheatre_barcode), (byte[]) Utils.drawableToArray(this.context, R.drawable.dionysoutheatre)));

			poilist.add(new POI("��������", "�� ��������, ����� ���������� �� ��������� ��������� ��� ������������� �����, ��������� ��� ������ ������ ��� ������ ��� ���������. ������������ ������ ��� ���� 421-406 �.�., ��������������� ��� ���������� ���, ��� ��������� ���� ��� ����� ��� ���� ����������� ���� ����� �������, �� �������� ''������ ���''. � ���� ���������� �� ''��������'' ���� ��� ��� �������� (1.26.5) ��� � �������� ���� ���������� �� �� ������ ������� ��� ������ �������, ��� ���������� ������ ��� ���� ����. ��� ����� ����� �� ���������� ������� ���������� ����� �� ''����'' � ''������� ����''. � ���������� ����� ��� ��������� �� ����� ��� ���������� ��� �������, ��� ����� ���� 3 �. �������� ��� ��������� �����, ���� ��� ���� ��� ������ ��������, ��� ������ �� �������� �� ��� ������������� ������. � ������ ���� ����������� ���� ����� ������� ��� ������������ �� ��������� ����� ��� �������, ��� ��� ������ �����, ��� ��������� �� ���������� �������, ���������� � ����������-�������� ��� ������� ����� ��� �������� ��� ��� ������, ������� ��� �������. ��� ����������, ������� �� �� ����, ��� � �������� ����, �� ���� ���� ��� ������. ���������, ���� ���� ���� ������� ��� �������� ���� ������, ��� ������ �� ������������� ��� �� ������, ���� � ����� ��� ������� ��� �� ���� ��� ������� ��� ����� ��� ������ ��� ��� ��������� ��� ��� ��������� ��� �����.", (float) 37.972028, (float) 23.725146, 0, "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=L&choe=ISO-8859-1&chl=geo%3A37.972028%2C23.725146%3Fq%3DErexthio", (byte[]) Utils.drawableToArray(this.context, R.drawable.erexthio_barcode), (byte[]) Utils.drawableToArray(this.context, R.drawable.erexthio)));

			poilist.add(new POI("����� ������ ��� �������", "�� ����� ������ ��� ������� ����� ������ ����� ��� �������� ��������, ��� ��������� ��� ����������� ������ ��� ��������� ��� ������.", (float) 37.971233, (float) 23.724240, 0, "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=L&choe=ISO-8859-1&chl=geo%3A37.971233%2C23.724240%3Fq%3DIrodou+Attikou+theatre", (byte[]) Utils.drawableToArray(this.context, R.drawable.irodouattikou_barcode), (byte[]) Utils.drawableToArray(this.context, R.drawable.irodouattikou)));

			poilist.add(new POI("����������", "� ����������, ���� ��������� ���� ����� ��� ������, ����������� ��� ����� ��� ������, ������ �� ���������� ��� ����������� ���������� ������������ ��� ������� ��� ���� ��� 5�� �.�. �����. � ����� ��� ���������� ��� ������������� �� �� �������� ���������� ������ ��� ������ ��� ��� ��������� ������ ��� ���������� ������ ��� �������� ��� ���� ��� ������� ��� ��������� ���������.", (float) 37.971538, (float) 23.726744, 0, "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=L&choe=ISO-8859-1&chl=geo%3A37.971538%2C23.726744%3Fq%3DPathenonas", (byte[]) Utils.drawableToArray(this.context, R.drawable.parthenonas_barcode), (byte[]) Utils.drawableToArray(this.context, R.drawable.parthenonas)));

			// insert POIs to db
			Iterator<POI> itr = poilist.iterator();
			while (itr.hasNext())
			{

				POI p = itr.next();

				insertData(p);
			}
		}	
	}

	
	public void insertData(POI poi)
	{
		ContentValues values;
		values = new ContentValues();
		values.put("title", poi.getTitle());
		values.put("description", poi.getDesc());
		values.put("activity", poi.getActivity());
		values.put("latitude", (int) (poi.getLat() * 1E6));
		values.put("longitude", (int) (poi.getLng() * 1E6));
		values.put("url", poi.getUrl());
		values.put("image", poi.getImage());
		values.put("barcode", poi.getBarcode());
		// getDatabase().insert("poi", null, values);
		this.db.insert("poi", null, values);
	}

	public void deleteData(long id)
	{
		this.db.delete("poi", "_id=?", new String[] { String.valueOf(id) });
	}

	public void updateData(long id, POI poi)
	{
		ContentValues values;
		values = new ContentValues();
		values.put("title", poi.getTitle());
		values.put("description", poi.getDesc());
		values.put("activity", poi.getActivity());
		values.put("latitude", (int) (poi.getLat() * 1E6));
		values.put("longitude", (int) (poi.getLng() * 1E6));
		values.put("url", poi.getUrl());
		values.put("image", poi.getImage());
		values.put("image_barcode", poi.getBarcode());
		this.db.update("poi", values, "_id=?", new String[] { String.valueOf(id) });
	}
	/**
	 * External storage availability getter
	 */
	public boolean getExternalStorageAvailable()
	{
		return externalStorageAvailable;
	}

	/**
	 * External storage writability getter
	 */
	public boolean getExternalStorageWriteable()
	{
		return externalStorageWriteable;
	}

	/**
	 * Preferences getter
	 */
	public SharedPreferences getPreferences()
	{
		return preferences;
	}

	/**
	 * Current application directory getter
	 */
	public String getAppDir()
	{
		return appDir;
	}

	/**
	 * Current location getter
	 */
	public Location getCurrentLocation()
	{
		return currentLocation;
	}

	/**
	 * Current location setter
	 */
	public void setCurrentLocation(Location currentLocation)
	{
		this.currentLocation = currentLocation;
	}

	/**
	 * Current azimuth orientation getter
	 */
	public float getCurrentAzimuth()
	{
		return azimuth;
	}

	/**
	 * Current azimuth orientation setter
	 */
	public void setCurrentAzimuth(float azimuth)
	{
		this.azimuth = azimuth;
	}
	
	/**
	 * Current POI location getter
	 */
	public POI getCurrentPOI()
	{
		return currentPOI;
	}

	/**
	 * Current POi location setter
	 */
	public void setCurrentPOI(POI poi)
	{
		this.currentPOI = poi;
	}

	/**
	 * Checking if external storage is available and writable
	 */
	private void setExternalStorageState()
	{
		// checking access to SD card
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			// We can read and write the media
			externalStorageAvailable = externalStorageWriteable = true;
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			// We can only read the media
			externalStorageAvailable = true;
			externalStorageWriteable = false;
		}
		else
		{
			// Something else is wrong. It may be one of many other states, but
			// all we need to know is we can neither read nor write
			externalStorageAvailable = externalStorageWriteable = false;
		}

	}

	/**
	 * Get application version name
	 * 
	 */
	public static String getVersionName(Context context)
	{
		PackageInfo packageInfo;
		try
		{
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Checking if Internet connection exists
	 */
	public boolean checkInternetConnection()
	{
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable()
				&& conMgr.getActiveNetworkInfo().isConnected())
		{
			return true;
		}
		else
		{
			Log.v(Constants.TAG, "Internet Connection Not Present");
			return false;
		}

	}

	public void loge(String message)
	{
		AppLog appLog = new AppLog(this);
		appLog.e(message);
	}

	public void logw(String message)
	{
		AppLog appLog = new AppLog(this);
		appLog.w(message);
	}

	public void logi(String message)
	{
		AppLog appLog = new AppLog(this);
		appLog.i(message);
	}

	public void logd(String message)
	{
		AppLog appLog = new AppLog(this);
		appLog.d(message);
	}

	/**
	 * Create application folders
	 */
	private void createFolderStructure()
	{
		createFolder(getAppDir());
		createFolder(getAppDir() + "/debug");
		createFolder(getAppDir() + "/logs");
		createFolder(getAppDir() + "/pois");
	}

	/**
	 * Create folder if not exists
	 * 
	 */
	private void createFolder(String folderName)
	{

		File folder = new File(folderName);

		// create output folder
		if (!folder.exists())
		{
			folder.mkdir();
		}

	}

}
