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
import com.sfecas.AthensTouristGps.webservice.WikiArticle;
import com.sfecas.AthensTouristGps.webservice.WikiLocator;

/**
 * @author Sfecas Efstathios
 */
public class BaseWikiArrayAdapter extends ArrayAdapter<WikiArticle> {

	private final Comparator<WikiArticle> wikiComparator = new Comparator<WikiArticle>() {
		@Override
		public int compare(WikiArticle w1, WikiArticle w2) 
		{		
			if (currentLocation == null)
				return 0;
	
			//compare based on the most near to the current location article
			float res1[] = Utils.GetDistance(
					currentLocation.getLatitude(),
					currentLocation.getLongitude(),
					Double.parseDouble(w1.getLatitude()),
					Double.parseDouble(w1.getLongitude()));
					
			float res2[] = Utils.GetDistance(
					currentLocation.getLatitude(),
					currentLocation.getLongitude(),
					Double.parseDouble(w2.getLatitude()),
					Double.parseDouble(w2.getLongitude()));
			
			if (res1 == null || res2 == null)
				return 0;
			
			return (res1[0] < res2[0] ? -1
					: (res1[0] ==  res2[0] ? 0 : 1));	
			
		}
	};

	private Context context;

	private ArrayList<WikiArticle> items;

	private Location currentLocation;
	
	public BaseWikiArrayAdapter(Context context, int textViewResourceId, Location currentLocation, ArrayList<WikiArticle> items) 
	{
		super(context, textViewResourceId, items);

		this.items = items;
		this.context = context;
		this.currentLocation = currentLocation;
	}

	 @Override
     public int getCount() 
	 {
         return items.size();
     }
	 
	public void setItems(ArrayList<WikiArticle> items) 
	{

		this.items = items;

	}

	public ArrayList<WikiArticle> getItems() {

		return this.items;
	}

	public Context getContext() {

		return this.context;
	}
	
	public void sortByWikiDistance() {

		this.sort(wikiComparator);
		this.notifyDataSetChanged();


	}
}
	