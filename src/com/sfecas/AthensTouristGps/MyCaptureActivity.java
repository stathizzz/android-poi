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
import java.util.ArrayList;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.sfecas.AthensTouristGps.App;
import com.sfecas.AthensTouristGps.helper.POI;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.R;
import com.google.*;
import com.google.android.*;
import com.google.zxing.client.android.*;
import com.google.zxing.*;

/**
 * @author Sfecas Efstathios
 */
public class MyCaptureActivity extends Activity
{
	App app;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zxingcapture);

		// reference to application object
		app = ((App) getApplicationContext());
	
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);

	}		

	protected void onResume()
	{
		super.onResume();

		// set locale to greek to avoid mantling with geo point commas
		Utils.setLocale(this, "en");
	}
	
	
	
	
	public void onPause()
	{
		Log.i(Constants.TAG, "MainActivity: onPause");
		
		//Intent intent = new Intent(this, ThirdMainActivity.class);	
        //startActivity(intent);
        
		super.onPause();	
	}
	
	/**
	 * Execute this when activity is hidden
	 */
	@Override
	protected void onDestroy()
	{
		app = null;
		super.onDestroy();
	}
	
	private double[] findCoordsRegex(String context)
	{
		try
		{
			Pattern p = Pattern.compile("([-+]?\\d+\\.\\d+).?([-+]?\\d+\\.\\d+)");
	        Matcher m = p.matcher(context);
	        m.find();  
	        
	        double[] p3 = { Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)) };
	        return p3;
		}
		catch (Exception ex)
		{
		 	Toast.makeText(MyCaptureActivity.this, R.string.fail_barcode_read, Toast.LENGTH_SHORT).show();
		 	return null;
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == 0) 
	    {
	        if (resultCode == RESULT_OK) 
	        {//
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            // Identify the POI from its latitude and longitude given on the DB
	            double coords[] = findCoordsRegex(contents);
	         
	            if (coords != null && coords.length == 2) 
	            {
	            	double lat_to = coords[0];
	            	double lng_to = coords[1];
	            	POI tmp = getPOIfromLocation(lat_to, lng_to);
					
	            	if (tmp == null)
	            	{
	            		Toast.makeText(MyCaptureActivity.this, R.string.no_poi_on_db, Toast.LENGTH_LONG).show();
	            		
	            		Intent intent2 = new Intent(this, ThirdMainActivity.class);
	            		
	            		startActivity(intent2);
	            		return;
	            	}
	            	
	            	app.setCurrentPOI(tmp);
	            	
					// using Bundle to pass POI id and location to main activity
					Intent intent2 = new Intent(this, ThirdMainActivity.class);
									
					intent2.putExtra("currentPoiId", tmp.getId());
					intent2.putExtra("latitude", lat_to);
					intent2.putExtra("longitude", lng_to);

					startActivity(intent2);
	            }
	        } 
	        else if (resultCode == RESULT_CANCELED) 
	        {
	            // Handle cancel
	        	Toast.makeText(MyCaptureActivity.this, R.string.fail_barcode_read, Toast.LENGTH_SHORT).show();
	        }
	    }
	}
	
	private POI getPOIfromLocation(double latitude, double longitude)
	{
		POI p = null;
		String sql;
		Cursor tmpCursor;
		sql = "SELECT * FROM poi WHERE latitude=" + latitude + " AND longitude=" + longitude + ";";
		tmpCursor = app.getDatabase().rawQuery(sql, null);
		boolean isAround = tmpCursor.moveToFirst();
		if (isAround) {
			p = new POI(tmpCursor.getString(tmpCursor.getColumnIndex("title")), tmpCursor.getString(tmpCursor.getColumnIndex("description")), tmpCursor.getFloat(tmpCursor.getColumnIndex("latitude")), tmpCursor.getFloat(tmpCursor.getColumnIndex("longitude")), tmpCursor.getInt(tmpCursor.getColumnIndex("activity")), tmpCursor.getString(tmpCursor.getColumnIndex("url")), tmpCursor.getBlob(tmpCursor.getColumnIndex("barcode")), tmpCursor.getBlob(tmpCursor.getColumnIndex("image")));
			p.setId(tmpCursor.getLong(tmpCursor.getColumnIndex("_id")));
		}
		tmpCursor.close();

		return p;
	}
		

}
