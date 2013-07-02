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
import java.util.Comparator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.widget.ArrayAdapter;

import com.sfecas.AthensTouristGps.helper.POI;
import com.sfecas.AthensTouristGps.helper.Utils;

/**
 * @author Sfecas Efstathios
 */
public class BasePoiArrayAdapter extends ArrayAdapter<POI> {

	private final Comparator<POI> distanceComparator = new Comparator<POI>() {
		@Override
		public int compare(POI wp1, POI wp2) 
		{		
			if (currentLocation == null)
				return 0;
			
			float[] res1 = Utils.GetDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), wp1.getLat(), wp1.getLng());
			float[] res2 = Utils.GetDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), wp2.getLat(), wp2.getLng());

			if (res1 == null || res2 == null)
				return 0;
			
			return (res1[0] < res2[0] ? -1
					: (res1[0] ==  res2[0] ? 0 : 1));		
		}
	};

	private Context context;

	private ArrayList<POI> items;

	private Location currentLocation;
	
	public BasePoiArrayAdapter(Context context, int textViewResourceId, Location currentLocation, ArrayList<POI> items) {

		super(context, textViewResourceId, items);

		this.items = items;
		this.context = context;
		this.currentLocation = currentLocation;
	}

	 @Override
     public int getCount() {
         return items.size();
     }
	 
	public void setItems(ArrayList<POI> items) {

		this.items = items;

	}

	public ArrayList<POI> getItems() {

		return this.items;
	}

	public Context getContext() {

		return this.context;
	}
	
	public void sortByDistance() {

		this.sort(distanceComparator);
		this.notifyDataSetChanged();

	}
	
	
}