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
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.sfecas.AthensTouristGps.App;
import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MapActivityV1 extends MapActivity
{

	private App app;

	private MapView mapView;

	private int mapMode;

	private int mode;

	private double latitude;

	private double longitude;

	/**
	 * Map overlay class
	 */
	class MyItemizedOverlay extends
			com.google.android.maps.ItemizedOverlay<OverlayItem>
	{

		private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();

		private Context mContext;

		public MyItemizedOverlay(Drawable defaultMarker, Context context)
		{
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		public MyItemizedOverlay(Drawable defaultMarker)
		{
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(OverlayItem overlay)
		{
			overlayItems.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i)
		{
			return overlayItems.get(i);
		}

		@Override
		public int size()
		{
			return overlayItems.size();
		}

		@Override
		protected boolean onTap(int index)
		{
			OverlayItem item = overlayItems.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}

	}

	/**
	 * Called when the activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		app = ((App) getApplicationContext());

		setContentView(R.layout.map_view);

		mapView = (MapView) findViewById(R.id.mapview);

		mapView.setSatellite(false);

		mapView.setBuiltInZoomControls(true);

		// getting extra data passed to the activity
		Bundle b = getIntent().getExtras();

		this.mode = b.getInt("mode");
		this.latitude = b.getDouble("latitude");
		this.longitude = b.getDouble("longitude");

		MapController mc = mapView.getController();

		double lat = (this.latitude > 1E6) ? this.latitude
				: this.latitude * 1E6;
		double lng = (this.longitude > 1E6) ? this.longitude
				: this.longitude * 1E6;
		GeoPoint p = new GeoPoint((int) (lat), (int) (lng));
		mc.animateTo(p);
		mc.setZoom(10);

		// ---Add a location marker---
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.map_pin);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

		MyItemizedOverlay itemizedOverlay = new MyItemizedOverlay(drawable, this);

		loadPOIs(itemizedOverlay);

		mapOverlays.add(itemizedOverlay);

		mapView.invalidate();

	}

	protected void loadPOIs(MyItemizedOverlay itemizedOverlay)
	{

		Cursor cursor = app.getDatabase().rawQuery("SELECT * FROM "
				+ Constants.POI_TABLE, null);
		cursor.moveToFirst();

		while (cursor.isAfterLast() == false)
		{

			GeoPoint point = new GeoPoint(cursor.getInt(cursor.getColumnIndex("latitude")), cursor.getInt(cursor.getColumnIndex("longitude")));

			String snippet = Utils.formatLat(cursor.getDouble(cursor.getColumnIndex("latitude")) / 1E6, Integer.parseInt(app.getPreferences().getString("coord_units", "0")))
					+ "\n"
					+ Utils.formatLng(cursor.getDouble(cursor.getColumnIndex("longitude")) / 1E6, Integer.parseInt(app.getPreferences().getString("coord_units", "0")));

			if (cursor.getString(cursor.getColumnIndex("description")) != null)
			{

				snippet = cursor.getString(cursor.getColumnIndex("description"))
						+ "\n" + snippet;

			}

			OverlayItem overlayitem = new OverlayItem(point, cursor.getString(cursor.getColumnIndex("title")), snippet);

			itemizedOverlay.addOverlay(overlayitem);

			cursor.moveToNext();
		}

		cursor.close();

	}

	/**
 *
 */
	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
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
				mapView.setSatellite(true);
				mapMode = Constants.MAP_SATELLITE;
			}
			else
			{
				mapView.setSatellite(false);
				mapMode = Constants.MAP_STREET;
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
