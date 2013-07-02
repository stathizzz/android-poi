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
import java.sql.Blob;
import java.util.Date;

import android.location.Location;
import android.widget.ImageView;

/**
 * @author Sfecas Efstathios
 */
public class POI
{
		private long id;
		private String description;
		private String title;
		private int activity = 0;
		private String url;
		private byte[] barcode_array, img_array;
		private ImageView img_barcode, image;
		private double latitude = 0, longitude = 0;
		private float distanceTo = 0;
		
		public POI(String title, String description, double latitude, double longitude, int activity, String url, byte[] barcodeblob, byte[] imgblob) {

			this.title = title;
			this.description = description;
			this.latitude = latitude;
			this.longitude = longitude;		
			this.activity = activity;
			this.url = url;
			this.barcode_array = barcodeblob;
			this.img_array = imgblob;
		}
		
		/**
		 * db poi record id
		 */
		
		public void setId(long id) {
			
			this.id = id;
		}

		public long getId() {
			
			return this.id;
		}

		public void setTitle(String title)
		{			
			this.title = title;
		}
		
		public String getTitle()
		{			
			return this.title;
		}
		
		public void setDesc(String description)
		{			
			this.description = description;
		}
		
		public String getDesc()
		{			
			return this.description;
		}
		
		public void setActivity(int activity)
		{			
			this.activity = activity;
		}
		
		public int getActivity()
		{			
			return this.activity;
		}
		
		public void setLat(float latitude) {
			
			this.latitude = latitude;
		}

		public double getLat() {
			
			return this.latitude;
		}

		public void setLng(float longitude) {
			
			this.longitude = longitude;
		}

		public double getLng() {
			
			return this.longitude;
		}
		
		public void setUrl(String url)
		{			
			this.url = url;
		}
		
		public String getUrl()
		{			
			return this.url;
		}
		
		public void setBarcode(byte[] barcode)
		{			
			this.barcode_array = barcode;
		}
		
		public byte[] getBarcode()
		{			
			return this.barcode_array;
		}
		
		public void setImage(byte[] image)
		{			
			this.img_array = image;
		}
		
		public byte[] getImage()
		{			
			return this.img_array;
		}
		

		/**
		 * returns simple Location object for calculating bearing and distance to
		 * this POI
		 */
		public Location getLocation() {

			Location loc = new Location("app");

			loc.setLatitude(latitude);
			loc.setLongitude(longitude);
	
			return loc;

		}

		public void setDistanceTo(float d) {
			this.distanceTo = d;
		}

		public float getDistanceTo() {
			return this.distanceTo;
		}
		
}
