package com.sfecas.AthensTouristGps.map;
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
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sfecas.AthensTouristGps.App;
import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.PoiListActivity;
import com.sfecas.AthensTouristGps.ThirdMainActivity;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.GooglePlayServicesUtil;
import android.support.v4.app.*;
public class PoiMapActivity extends FragmentActivity
{

	public class MapItem
	{
		public String title;
		public String snippet;
		public LatLng point;

		public MapItem(String title, String snippet, LatLng point)
		{
			this.title = title;
			this.snippet = snippet;
			this.point = point;
		}

	}

	private App app;

	// private Fragment mapView;
	private GoogleMap mapView;

	private Marker currentmark;

	private int mapMode;

	private int mode;

	private double latitude;

	private double longitude;

	/**
	 * Called when the activity is first created
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		app = ((App) getApplicationContext());

		try
		{		    
			setContentView(R.layout.mapview);
				
			android.app.Fragment temp = (android.app.Fragment) getFragmentManager().findFragmentById(R.id.mapview);
			
			mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapview)).getMap();
	
			mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
		catch (Exception ex)
		{
			//eat it
			startActivity(new Intent(this, ThirdMainActivity.class));
			return;
		}
		
		// getting extra data passed to the activity
		Bundle b = getIntent().getExtras();

		if (b == null)
		{
			Location cl = app.getCurrentLocation();

			if (cl != null)
			{
				LatLng p = new LatLng(cl.getLatitude(), cl.getLongitude());
				currentmark = mapView.addMarker(new MarkerOptions().position(p).title("My position"));

				// Move the camera instantly to poi with a zoom of 15.
				mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
			}
				
		}
		else {
			this.mode = b.getInt("mode");
			this.latitude = b.getDouble("latitude");
			this.longitude = b.getDouble("longitude");

			double lat = (this.latitude > 1E6) ? this.latitude / 1E6
					: this.latitude;
			double lng = (this.longitude > 1E6) ? this.longitude / 1E6
					: this.longitude;

			LatLng p = new LatLng(lat, lng);
			currentmark = mapView.addMarker(new MarkerOptions().position(p).title("My position"));

			// Move the camera instantly to poi with a zoom of 15.
			mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
		}
		
		// Zoom in, animating the camera.
		mapView.animateCamera(CameraUpdateFactory.zoomTo(9), 2000, null);

		List<MapItem> itemizedOverlay = new ArrayList<MapItem>();

		loadPOIs(itemizedOverlay);

		loadMarkers(itemizedOverlay);

	}

	protected void loadMarkers(List<MapItem> itemizedOverlay)
	{
		for (MapItem item : itemizedOverlay)
		{
			mapView.addMarker(new MarkerOptions().position(item.point).title(item.title).snippet(item.snippet));
		}
	}

	protected void loadPOIs(List<MapItem> itemizedOverlay)
	{

		Cursor cursor = app.getDatabase().rawQuery("SELECT * FROM "	+ Constants.POI_TABLE, null);
		cursor.moveToFirst();

		while (cursor.isAfterLast() == false)
		{

			double lat = (cursor.getDouble(cursor.getColumnIndex("latitude")) > 1E6) ? cursor.getDouble(cursor.getColumnIndex("latitude")) / 1E6
					: cursor.getDouble(cursor.getColumnIndex("latitude"));
			double lng = (cursor.getDouble(cursor.getColumnIndex("longitude")) > 1E6) ? cursor.getDouble(cursor.getColumnIndex("longitude")) / 1E6
					: cursor.getDouble(cursor.getColumnIndex("longitude"));
			
			LatLng point = new LatLng(lat, lng);

			String snippet = Utils.formatLat(lat, Integer.parseInt(app.getPreferences().getString("coord_units", "0")))
					+ "\n"
					+ Utils.formatLng(lng, Integer.parseInt(app.getPreferences().getString("coord_units", "0")));

			if (cursor.getString(cursor.getColumnIndex("description")) != null)
			{
				snippet = cursor.getString(cursor.getColumnIndex("description"))
						+ "\n" + snippet;
			}

			String title = cursor.getString(cursor.getColumnIndex("title"));

			itemizedOverlay.add(new MapItem(title, snippet, point));

			cursor.moveToNext();
		}

		cursor.close();

	}

	/**
	 * onCreateOptionsMenu handler
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		// options menu only in track mode
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poi_map_menu, menu);

		return true;
	}

	/**
	 * Changes activity menu on the fly
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{

		MenuItem mapMenuItem = menu.findItem(R.id.mapMode);
		if (mapMode == Constants.MAP_STREET)
		{
			mapMenuItem.setTitle(R.string.satellite);
		}
		else
		{
			mapMenuItem.setTitle(R.string.street);
		}

		return true;
	}

	/**
	 * Process main activity menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int itemId = item.getItemId();
		if (itemId == R.id.mapMode) {
			if (mapMode == Constants.MAP_STREET)
			{
				mapView.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				mapMode = Constants.MAP_SATELLITE;
			}
			else
			{
				mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				mapMode = Constants.MAP_STREET;
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
