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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
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
import android.location.Location;
import android.os.Bundle;

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

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.AdapterContextMenuInfo;

import com.sfecas.AthensTouristGps.helper.POI;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.map.MapActivityV1;
import com.sfecas.AthensTouristGps.map.PoiMapActivity;
import com.sfecas.AthensTouristGps.service.*;
import com.sfecas.AthensTouristGps.view.CompassImage;
import com.sfecas.AthensTouristGps.R;

/**
 * @author Sfecas Efstathios
 * POI list activity
 */
public class PoiListActivity extends ListActivity
{

	/**
	 * Reference to app object
	 */
	private App app;

	private String importPOIsFileName;

	private PoiArrayAdapter poiArrayAdapter;

	private ArrayList<POI> pois;

	private Location currentLocation;

	/**
	 * azimuth (received from orientation sensor)
	 */
	private float azimuth = 0;

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

			Log.d(Constants.TAG, "POIListActivity: LOCATION BROADCAST MESSAGE RECEIVED");

			Bundle bundle = intent.getExtras();
			currentLocation = (Location) bundle.getParcelable("location");

			poiArrayAdapter.sortByDistance();
			// poiArrayAdapter.notifyDataSetChanged();
		}
	};

	/**
	 * Compass updates broadcast receiver
	 */
	protected BroadcastReceiver compassBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();
			setAzimuth(bundle.getFloat("azimuth"));
			
			if (app != null)
				app.setCurrentAzimuth(bundle.getFloat("azimuth"));
		}
	};

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

			String elevationUnit = app.getPreferences().getString("elevation_units", "m");

			POI wp = this.getItems().get(position);
			if (wp != null)
			{
				if (currentLocation != null)
				{
					float[] distanceTo = Utils.GetDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), wp.getLat(), wp.getLng());

					String distanceUnit = app.getPreferences().getString("distance_units", "km");

					distStr = Utils.formatDistance(distanceTo[0], distanceUnit)
							+ " "
							+ Utils.getLocalizedDistanceUnit(PoiListActivity.this, distanceTo[0], distanceUnit);

					wp.setDistanceTo(distanceTo[0]);

					newBearing = currentLocation.bearingTo(wp.getLocation());

					if ((int) newBearing < 0)
					{
						newBearing = 360 - Math.abs((int) newBearing);
					}

					newAzimuth = newBearing
							- getAzimuth()
							- (float) Utils.getDeviceRotation(PoiListActivity.this);
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
					poiDetails.setText(Utils.formatLat(wp.getLocation().getLatitude(), Integer.parseInt(app.getPreferences().getString("coord_units", "0")))
							+ "|"
							+ Utils.formatLng(wp.getLocation().getLongitude(), Integer.parseInt(app.getPreferences().getString("coord_units", "0")))
							+ "|"
							+ Utils.formatNumber(wp.getLocation().getAltitude(), 0)
							+ ""
							+ Utils.getLocalizedElevationUnit(PoiListActivity.this, elevationUnit)
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

	// ////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Called when the activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		app = (App) getApplication();

		// initializing with last known location, so we can calculate distance
		// to POIs
		currentLocation = app.getCurrentLocation();
	
		serviceConnection = new AppServiceConnection(this, appServiceConnectionCallback);

		registerForContextMenu(this.getListView());

		pois = Utils.updatePOIsArray(app);

		poiArrayAdapter = new PoiArrayAdapter(this, R.layout.poi_list_item, currentLocation, pois);

		setListAdapter(poiArrayAdapter);

	}

	private Runnable appServiceConnectionCallback = new Runnable()
	{

		@Override
		public void run()
		{

			AppService appService = serviceConnection.getService();

			if (appService == null)
			{
				Toast.makeText(PoiListActivity.this, R.string.gps_service_not_connected, Toast.LENGTH_SHORT).show();
				return;
			}

			// this activity is started by MainActivity which is always
			// listening for location updates

			// by setting gpsInUse to true we insure that listening will not
			// stop in AppService.stopLocationUpdatesThread
			appService.setGpsInUse(true);

			// this activity requires compass data
			//appService.startSensorUpdates();

		}
	};

	/**
	 * Edit POI
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{

		final long poiId = poiArrayAdapter.getItem((int) id).getId();

		this.updatePOI(poiId);

	}

	/**
	 * 
	 */
	@Override
	public void onPause()
	{

		unregisterReceiver(locationBroadcastReceiver);
		unregisterReceiver(compassBroadcastReceiver);

		AppService appService = serviceConnection.getService();
		// unbind AppService
		serviceConnection.unbindAppService();
		if (appService != null)
		{

			// stop location updates when not recording track
			appService.stopLocationUpdates();
			//appService.stopSensorUpdates();
		}

		super.onPause();
	}

	/**
	 * 
	 */
	@Override
	protected void onDestroy()
	{

		if (pois != null)
		{
			pois.clear();
			pois = null;
		}

		serviceConnection = null;
		app = null;
		super.onDestroy();

	}

	/**
	 * onResume event handler
	 */
	@Override
	protected void onResume()
	{

		super.onResume();

		// set locale to greek to avoid mantling with geo point commas
		Utils.setLocale(this, "en");

		// registering receiver for compass updates
		registerReceiver(compassBroadcastReceiver, new IntentFilter(Constants.ACTION_COMPASS_UPDATES));

		// registering receiver for location updates
		registerReceiver(locationBroadcastReceiver, new IntentFilter(Constants.ACTION_LOCATION_UPDATES));

		// bind to AppService
		// appServiceConnectionCallback will be called once bound
		serviceConnection.bindAppService();
	}

	/**
	 * onCreateOptionsMenu handler
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poi_menu, menu);
		return true;
	}

	/**
     * 
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int itemId = item.getItemId();
		if (itemId == R.id.deleteMenuItem) {
			// clear all POIs with confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.are_you_sure).setCancelable(true).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{

					// delete all POIs
					String sql = "DELETE FROM poi";
					app.getDatabase().execSQL(sql);

					pois = Utils.updatePOIsArray(app);// cursor.requery();
					poiArrayAdapter.setItems(pois);
					poiArrayAdapter.notifyDataSetChanged();

					Toast.makeText(PoiListActivity.this, R.string.all_pois_deleted, Toast.LENGTH_SHORT).show();

				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		} else if (itemId == R.id.importMenuItem) {
			this.importFromXMLFile();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * Create context menu for selected item
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{

		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle(getString(R.string.poi));
		menu.add(Menu.NONE, 0, 0, R.string.edit);
		menu.add(Menu.NONE, 1, 1, R.string.delete);
		menu.add(Menu.NONE, 2, 2, R.string.email_to);
		menu.add(Menu.NONE, 3, 3, R.string.show_on_map);
		menu.add(Menu.NONE, 4, 4, R.string.navigate);
	}

	/**
	 * Handle activity menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		POI poi = poiArrayAdapter.getItem((int) info.id);
		final long poiId = poiArrayAdapter.getItem((int) info.id).getId();

		switch (item.getItemId())
		{

		case 0:
			// update POI in DB
			updatePOI(poi.getId());
			return true;
		case 1:
			// delete one POI with confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure?").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// delete poi from db
					String sql = "DELETE FROM poi WHERE _id=" + poiId + ";";
					app.getDatabase().execSQL(sql);

					// cursor.requery();
					pois = Utils.updatePOIsArray(app);
					poiArrayAdapter.setItems(pois);
					poiArrayAdapter.notifyDataSetChanged();

					Toast.makeText(PoiListActivity.this, R.string.poi_deleted, Toast.LENGTH_SHORT).show();
				}
			}).setNegativeButton("No", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case 2:
			// email POI data using default email client

			double lat1 = poi.getLat();
			double lng1 = poi.getLng();

			String messageBody = getString(R.string.title) + ": "
					+ poi.getTitle() + "\n\n" + getString(R.string.lat) + ": "
					+ Utils.formatLat(lat1, 0) + "\n" + getString(R.string.lng)
					+ ": " + Utils.formatLng(lng1, 0) + "\n\n"
					+ "http://maps.google.com/?ll=" + lat1 + "," + lng1
					+ "&z=10";

			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject_poi));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageBody);
			this.startActivity(Intent.createChooser(emailIntent, getString(R.string.sending_email)));
			return true;
		case 3:
			// showing POI on the Google map
			showOnMap(poi);
			return true;
		case 4:
			app.setCurrentPOI(poi);

			Intent intent = new Intent(this, ThirdMainActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * Update POI in the database
	 * 
	 * @param poiId
	 * @param title
	 * @param lat
	 * @param lng
	 */
	protected void updatePOI(long poiId)
	{

		Context context = this;

		// update POI in DB
		String sql = "SELECT * FROM poi WHERE _id=" + poiId + ";";
		Cursor wpCursor = app.getDatabase().rawQuery(sql, null);
		wpCursor.moveToFirst();

		String title = wpCursor.getString(wpCursor.getColumnIndex("title"));

		String descr = wpCursor.getString(wpCursor.getColumnIndex("description"));

		Double lat = wpCursor.getDouble(wpCursor.getColumnIndex("latitude")) / 1E6;
		Double lng = wpCursor.getDouble(wpCursor.getColumnIndex("longitude")) / 1E6;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.add_poi_dialog, (ViewGroup) findViewById(R.id.add_poi_dialog_layout_root));

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle("Edit POI data");
		builder.setView(layout);

		final EditText wpTitle = (EditText) layout.findViewById(R.id.poiTitleInputText);
		wpTitle.setText(title);

		final EditText wpDescr = (EditText) layout.findViewById(R.id.waypointDescriptionInputText);
		wpDescr.setText(descr);

		final EditText wpLat = (EditText) layout.findViewById(R.id.waypointLatInputText);
		wpLat.setText(Double.toString(lat));

		final EditText wpLng = (EditText) layout.findViewById(R.id.waypointLngInputText);
		wpLng.setText(Double.toString(lng));

		final String wpId = Long.toString(poiId);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{

				// POI title from input dialog
				String titleStr = wpTitle.getText().toString().trim();
				String descrStr = wpDescr.getText().toString().trim();
				int latE6 = (int) (Double.parseDouble(wpLat.getText().toString()) * 1E6);
				int lngE6 = (int) (Double.parseDouble(wpLng.getText().toString()) * 1E6);

				if (titleStr.equals(""))
				{
					Toast.makeText(PoiListActivity.this, R.string.poi_title_required, Toast.LENGTH_SHORT).show();
					return;
				}

				ContentValues values = new ContentValues();
				values.put("title", titleStr);
				values.put("description", descrStr);
				values.put("latitude", latE6);
				values.put("longitude", lngE6);

				try
				{
					app.getDatabase().update("poi", values, "_id=" + wpId, null);
					Toast.makeText(PoiListActivity.this, R.string.poi_updated, Toast.LENGTH_SHORT).show();

					// cursor.requery();
					pois = Utils.updatePOIsArray(app);
					poiArrayAdapter.setItems(pois);
					poiArrayAdapter.notifyDataSetChanged();

				}
				catch (SQLiteException e)
				{
					Log.w(Constants.TAG, "SQLiteException: " + e.getMessage(), e);
				}

			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});

		AlertDialog dialog = builder.create();

		dialog.show();

	}

	private boolean inPOIsArray(String title)
	{

		for (Iterator<POI> it = pois.iterator(); it.hasNext();)
		{

			POI curWp = (POI) it.next();

			if (curWp.getTitle().equals(title))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Imports POIs from gpx file
	 */
	protected void importFromXMLFile()
	{

		File importFolder = new File(app.getAppDir() + "/pois");

		final String importFiles[] = importFolder.list();

		if (importFiles == null || importFiles.length == 0)
		{
			Toast.makeText(PoiListActivity.this, "Import folder is empty", Toast.LENGTH_SHORT).show();
			return;
		}

		// 1st file is selected by default
		importPOIsFileName = importFiles[0];

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setSingleChoiceItems(importFiles, 0, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				importPOIsFileName = importFiles[whichButton];
			}
		});

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				try
				{
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					File file = new File(app.getAppDir() + "/pois", importPOIsFileName);

					Document doc = db.parse(file);
					doc.getDocumentElement().normalize();

					NodeList poisList = doc.getElementsByTagName("POI");

					boolean updateRequired = false;

					for (int i = 0; i < poisList.getLength(); i++)
					{
						int latE6 = (int) (Double.parseDouble(((Element) poisList.item(i)).getAttribute("latitude")) * 1E6);
						int lngE6 = (int) (Double.parseDouble(((Element) poisList.item(i)).getAttribute("longitude")) * 1E6);
						String title = "";
						String desc = "";
						String url = "";
						byte[] barcode = null, image = null;
						int activity = 0;
						long time = 0;

						Node item = poisList.item(i);

						NodeList properties = item.getChildNodes();
						for (int j = 0; j < properties.getLength(); j++)
						{
							Node property = properties.item(j);
							String name = property.getNodeName();

							if (name.equalsIgnoreCase("activity")
									&& property.getFirstChild() != null)
							{
								activity = Integer.parseInt(property.getFirstChild().getNodeValue());
							}
							if (name.equalsIgnoreCase("time")
									&& property.getFirstChild() != null)
							{
								time = (new SimpleDateFormat("yyyy-MM-dd H:mm:ss")).parse(property.getFirstChild().getNodeValue()).getTime();
							}
							if (name.equalsIgnoreCase("title")
									&& property.getFirstChild() != null)
							{
								title = property.getFirstChild().getNodeValue();
							}
							if (name.equalsIgnoreCase("description")
									&& property.getFirstChild() != null)
							{
								desc = property.getFirstChild().getNodeValue();
							}
							if (name.equalsIgnoreCase("url")
									&& property.getFirstChild() != null)
							{
								url = property.getFirstChild().getNodeValue();
							}
							if (name.equalsIgnoreCase("barcode")
									&& property.getFirstChild() != null)
							{
								String barcode_data =  property.getFirstChild().getNodeValue();
								barcode = new byte[barcode_data.length()];
								barcode = barcode_data.getBytes();
								
							}
							if (name.equalsIgnoreCase("image")
									&& property.getFirstChild() != null)
							{
								String image_data =  property.getFirstChild().getNodeValue();
								image = new byte[image_data.length()];
								image = image_data.getBytes();
							}
						}

						// adding imported POI to db
						if (!inPOIsArray(title))
						{
							try
							{
								app.insertData(new POI(title, desc, latE6, lngE6, activity, url, barcode, image));
							}
							catch (SQLiteException e)
							{
								Log.e(Constants.TAG, "SQLiteException: "
										+ e.getMessage(), e);
							}

							// if at least one record added, update POIs list
							updateRequired = true;
						}
					}

					if (updateRequired)
					{
						pois = Utils.updatePOIsArray(app);
						poiArrayAdapter.setItems(pois);
						poiArrayAdapter.notifyDataSetChanged();
					}

					Toast.makeText(PoiListActivity.this, R.string.import_completed, Toast.LENGTH_SHORT).show();

				}
				catch (IOException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}
				catch (ParserConfigurationException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}
				catch (ParseException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}
				catch (SAXException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}

				dialog.dismiss();
			}
		}).setTitle(R.string.select_file).setCancelable(true);

		AlertDialog alert = builder.create();

		alert.show();

	}

	/**
	 * Show current POI on map
	 * 
	 * @param poiId
	 *            Id of the requested POI
	 */
	protected void showOnMap(POI poi)
	{

		Intent i = new Intent(this, PoiMapActivity.class);

		// using Bundle to pass track id into new activity
		Bundle b = new Bundle();
		b.putInt("mode", Constants.SHOW_POI);
		b.putDouble("latitude", poi.getLat());
		b.putDouble("longitude", poi.getLng());

		i.putExtras(b);
		startActivity(i);

	}

	public void setAzimuth(float a)
	{
		azimuth = a;
	}

	public float getAzimuth()
	{
		return azimuth;
	}

}
