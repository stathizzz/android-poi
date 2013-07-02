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
import android.content.Context;

public class ArticleUtilizer extends WikiUtilizer
{	
	public enum ArticleType
	{
		adm1st,	  //Administrative unit of country, 1st level (province, state), see table, e.g. U.S. states	1:1,000,000
		adm2nd,	  //Administrative unit of country, 2nd level, see table, e.g. county (United States)	1:300,000
		adm3rd,	  //Administrative unit of country, 3rd level, see table	1:100,000
		airport,  //airports and airbases	1:30,000
		city,	  //cities, towns, villages, hamlets, suburbs, subdivisions, neighborhoods, and other human settlements (including unincorporated and/or abandoned ones) with unspecified population.	These are treated as minor cities.	1:100,000
		country,  //(e.g. "type:country")	1:10,000,000
		edu, 	  //schools, colleges, and universities	1:10,000
		event,	  //one-time or regular events and incidents that occurred at a specific location, including battles, earthquakes, festivals, and shipwrecks	1:50,000
		forest,   //forests and woodlands	1:50,000
		glacier,  //glaciers and icecaps	1:50,000
		isle,     //islands and isles	1:100,000
		landmark, //buildings (including churches, factories, museums, theatres, and power plants but excluding schools and railway stations), caves, cemeteries, cultural landmarks, geologic faults, headlands, intersections, mines, ranches, roads, structures (including antennas, bridges, castles, dams, lighthouses, monuments, and stadiums), tourist attractions, valleys, and other points of interest	1:10,000
		mountain, //peaks, mountain ranges, hills, submerged reefs, and seamounts	1:100,000
		pass,     //mountain passes	1:10,000
		railwaystation,//stations, stops, and maintenance areas of railways and trains, including railroad, metro, rapid transit, underground, subway, elevated railway, etc.	1:10,000
		river,    //rivers, canals, creeks, brooks, and streams, including intermittent ones	1:100,000
		satellite,//geo-stationary satellites	1:10,000,000
		waterbody,//bays, fjords, lakes, reservoirs, ponds, lochs, loughs, meres, lagoons, estuaries, inland seas, and waterfalls	1:100,000
		camera	   //To indicate the location of where a specific image was taken. This type is used by coordinate templates on File pages.
	};	
	public class Lat
	{
		double lat; // (required) - latitude in decimal degree format.
		public Lat(double lat)
		{
			this.lat = lat;
		}
	};
	public class Lng
	{
		double lng; // (required) - longitude in decimal degree format.
		public Lng(double lng)
		{
			this.lng = lng;
		}
	};
	public class Radius
	{
		int radius; // (optional) - the radius (in metres) to search within. There is a maximum radius of 20km and it will default to 250m if no radius is supplied.
		public Radius(int radius)
		{
			this.radius = radius;
		}
	}	
	public class Limit
	{
		int limit;  // (optional) - the number of results you want to return. There is a maximum limit of 50 results although you can paginate (see below). This will default to 50 if no lower figure is sent.
		public Limit(int limit)
		{
			this.limit = limit;
		}
	}	
	public class Offset
	{
		int offset; // (optional) - the offset from the first result returned. E.g. if you wanted to view results 51-100 you would supply an offset of 50. There is no maximum for this parameter. The default is 0.
		public Offset(int offset)
		{
			this.offset = offset;
		}
	}
	public class Type
	{
		ArticleType type;
		public Type(ArticleType type)
		{
			this.type = type;
		}
	}
	public class Title
	{
		String title;  // (optional) - does a partial word match on the title of the article. By default, all articles are returned.
		public Title(String title)
		{
			this.title = title;
		}
	}
	
	public static final String ArticleURL = "http://api.wikilocation.org/articles";
	
	public ArticleUtilizer()
	{
		super();
	}
	
	@Override
	public String FormatUrl(Object... params)
	{	
		 super.FormatUrl(params);
		
		 StringBuilder s = new StringBuilder(ArticleURL);
		 s.append("?");
		 
		 for (int i = 0; i < params.length; i++) 
		 {
		      if (params[i] instanceof Type)
		      {
		    	  s.append("type=");  
		    	  s.append(((Type)params[i]).type.name());  
		      }
		      else if (params[i] instanceof Lat)
		      {
		    	  s.append("lat=");  
		    	  s.append(String.valueOf(((Lat)params[i]).lat)); 
		      }
		      else if (params[i] instanceof Lng)
		      {
		    	  s.append("lng=");  
		    	  s.append(String.valueOf(((Lng)params[i]).lng)); 
		      }
		      else if (params[i] instanceof Radius)
		      {
		    	  s.append("radius=");  
		    	  s.append(String.valueOf(((Radius)params[i]).radius)); 
		      }
		      else if (params[i] instanceof Limit)
		      {
		    	  s.append("limit=");  
		    	  s.append(String.valueOf(((Limit)params[i]).limit)); 
		      }
		      else if (params[i] instanceof Offset)
		      {
		    	  s.append("offset=");  
		    	  s.append(String.valueOf(((Offset)params[i]).offset)); 
		      }
		      else if (params[i] instanceof Title)
		      {
		    	  s.append("title=");  
		    	  s.append(((Title)params[i]).title); 
		      }
		      else
		    	  continue;
		      
		      s.append("&");
		 }
		 
		 return s.toString();
	}
	
}
