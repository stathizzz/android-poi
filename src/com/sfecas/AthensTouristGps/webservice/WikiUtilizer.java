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
public class WikiUtilizer
{

	public class Locale
	{
		String locale;	//(optional) - you can use this parameter in order to search for articles from a different wikipedia.org locale. For instance, 'de' would search the German version (de.wikipedia.org). Defaults to 'en' if none specified. View the full list of supported locales.
		public Locale(String locale)
		{
			this.locale = locale;
		}
	}
	
	public class Format
	{
		String format;	//(optional) - the desired output format; either 'json' or 'xml'. Defaults to 'json' if none specified or if entered incorrectly.
		public Format(String format)
		{
			this.format = format;
		}
	}
	
	public class Jsonp
	{
		String jsonp;	//(optional) - if you are using the 'json' format, you can choose to supply a JSONP callback via this parameter. This will cause the output to be wrapped inside a callback function (the name of which you supply as the value of the parameter) for cross-domain usage.
		public Jsonp(String jsonp)
		{
			this.jsonp = jsonp;
		}
	}
	
	public class Debug
	{
		boolean debug;	//(optional) - if set to 'true', then it will suppress the application/x-json header on any returned json stream. This is useful for debugging in a browser as it means the json stream will appear as text in the browser window rather than as a download prompt. Defaults to 'false'.
		public Debug(boolean debug)
		{
			this.debug = debug;
		}
	}
	
	public WikiUtilizer()
	{
	
	}
	
	public String FormatUrl(Object... params)
	{
		 String s = new String();
		 
		 for (int i = 0; i < params.length; i++) 
		 {
		      if (params[i] instanceof WikiUtilizer.Locale)
		      {
		    	  s.concat("locale=");  
		    	  s.concat(((WikiUtilizer.Locale)params[i]).locale);  
		      }
		      else if (params[i] instanceof WikiUtilizer.Format)
		      {
		    	  s.concat("format=");  
		    	  s.concat(((WikiUtilizer.Format)params[i]).format); 
		      }
		      else if (params[i] instanceof WikiUtilizer.Jsonp)
		      {
		    	  s.concat("jsonp=");  
		    	  s.concat(((WikiUtilizer.Jsonp)params[i]).jsonp); 
		      }
		      else if (params[i] instanceof WikiUtilizer.Debug)
		      {
		    	  s.concat("radius=");  
		    	  s.concat(String.valueOf(((WikiUtilizer.Debug)params[i]).debug)); 
		      }
		      else
		    	  continue;
		      
		      s.concat("&");
		 }
		 
		 return s;
	}
}
