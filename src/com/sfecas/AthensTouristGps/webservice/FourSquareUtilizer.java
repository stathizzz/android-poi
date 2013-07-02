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

public class FourSquareUtilizer extends WikiUtilizer
{
	
	public class Radius
	{
		int radius; //(optional) - the radius (in metres) to search within. There is a maximum radius of 5km and it will default to 250m if no radius is supplied.
	};
	
	public class Exact
	{
		boolean exact; //(optional) - defaults to false. If set to true, it will only return articles with the exact same name as the Foursquare venue (e.g. the spot for "Big Ben" with a radius of 250m returns several articles - specifying "exact" ensures that only an article with the title "Big Ben" is returned). This method is useful as some Foursquare venues may be incorrectly placed by a small distance meaning that simply taking the first result as the article for that location may not be correct. Be advised that "exact" works by matching the titles so if they are not exact then it will not work (e.g. "St David's Station" and "St Davids Station" would not match).

	};
	
	private String id;
	
	public static final String FourSquareURL = "http://api.wikilocation.org/foursquare";
	
	public FourSquareUtilizer(String id)
	{
		super();
		this.id = id;
	}
	
	@Override
	public String FormatUrl(Object... params)
	{	
		 super.FormatUrl(params);
		
		 String s = new String(FourSquareURL);
		 s.concat("/");
		 s.concat(id);
		 s.concat("?");
		 
		 for (int i = 0; i < params.length; i++) 
		 {
		      if (params[i] instanceof Radius)
		      {
		    	  s.concat("lat=");  
		    	  s.concat(String.valueOf(((Radius)params[i]).radius)); 
		      }		     
		      else if (params[i] instanceof Exact)
		      {
		    	  s.concat("offset=");  
		    	  s.concat(String.valueOf(((Exact)params[i]).exact)); 
		      }
		      else
		    	  continue;
		      
		      s.concat("&");
		 }
		 
		 return s;
	}
}