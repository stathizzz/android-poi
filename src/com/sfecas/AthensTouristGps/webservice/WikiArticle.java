package com.sfecas.AthensTouristGps.webservice;
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
import android.os.Parcel;
import android.os.Parcelable;

public class WikiArticle implements Parcelable
{
	public WikiArticle()
	{ }
	
	private WikiArticle(Parcel in)
	{
		id = in.readString();
		distance = in.readString();
		lat = in.readString();
		lng = in.readString();
		title = in.readString();
		url = in.readString();
	}

	public static final Parcelable.Creator<WikiArticle> CREATOR = new Parcelable.Creator<WikiArticle>()
	{
		public WikiArticle createFromParcel(Parcel in)
		{
			return new WikiArticle(in);
		}
		
		public WikiArticle[] newArray(int size) {
            return new WikiArticle[size];
        }
	};

	String id;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	String lat;

	public String getLatitude()
	{
		return lat;
	}

	public void setLatitude(String lat)
	{
		this.lat = lat;
	}

	String lng;

	public String getLongitude()
	{
		return lng;
	}

	public void setLongitude(String lng)
	{
		this.lng = lng;
	}

	String title;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	String url;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	String distance;

	public String getDistance()
	{
		return distance;
	}

	public void setDistance(String distance)
	{
		this.distance = distance;
	}

	@Override
	public int describeContents()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1)
	{
		arg0.writeString(this.id);
		arg0.writeString(this.distance);
		arg0.writeString(this.lat);
		arg0.writeString(this.lng);
		arg0.writeString(this.title);
		arg0.writeString(this.url);
	}
};
