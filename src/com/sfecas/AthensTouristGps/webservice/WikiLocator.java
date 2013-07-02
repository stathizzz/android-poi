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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sfecas.AthensTouristGps.Constants;
import com.sfecas.AthensTouristGps.R;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.content.ContextWrapper;

public class WikiLocator 
{
	private Context context;
	
	private ConcurrentLinkedQueue<WikiArticle[]> queue;
	
	private static WikiLocator instance;
	
	public static synchronized WikiLocator getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new WikiLocator(context);
		}
		return instance;
	}
	
	private WikiLocator(Context context)
	{
		this.context = context;
		queue = new ConcurrentLinkedQueue<WikiArticle[]>();
	}
	
	public void DoRequest(String url)
	{
		new RequestTask(this.context).execute(url);
	}
	
	public WikiArticle[] ReadLastResponse()
	{
		if (!queue.isEmpty())
			return queue.peek();
		
		return null;
	}
	public ArrayList<WikiArticle[]> ReadAllResponses()
	{
		ArrayList<WikiArticle[]> tmp = new ArrayList<WikiArticle[]>(queue.size());
		
		for (int i=0; i < queue.size(); i++)
		{
			tmp.add(queue.peek());
		}
		return tmp;		
	}
	public void ClearAllResponses()
	{
		queue.clear();
	}
	public class RequestTask extends AsyncTask<String, String, String>
	{
		
		Context context;
		
		public RequestTask(Context context)
		{
			this.context = context;
		}
		
	    @Override
	    protected String doInBackground(String... uri) 
	    {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try 
	        {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            
	            StatusLine statusLine = response.getStatusLine();
	            
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK)
	            {
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();              
	            }
	            else
	            {
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        }
	        catch (ClientProtocolException e) 
	        {
	        	Log.i(Constants.TAG, "ERROR on HTTP request! Message:" + e.getMessage());
	        }
	        catch (IOException e) 
	        {
	        	Log.i(Constants.TAG, "ERROR on HTTP request! Message:" + e.getMessage());
	            
	        } 
	       
	        return responseString;
	    }

	    @Override
	    protected void onPostExecute(String result) 
	    {
	        super.onPostExecute(result);
	        	        
	        //Send response to main app..
	        Gson gson = new Gson(); 
	        
	        try 
	        { 
	        	String jsonstr = result.substring(result.indexOf('['), result.indexOf(']')+1).replace('"', '\'');        	

	        	WikiArticle[] resp = gson.fromJson(jsonstr, WikiArticle[].class);
	            	        	
	            queue.add(resp);
	            
	            networkUpdate(this.context, resp, Constants.ACTION_NETWORK_UPDATES);
	            
	            return;
	        } 
	        catch (Exception e) 
	        {
	            Log.i("json array","While getting server response server generate error. ");
	        }
	    }
	    
	    /**
    	 * Broadcasting location update
    	 */
    	private void networkUpdate(Context context, WikiArticle[] array, String action)
    	{
    		// let's broadcast wiki article data to any activity waiting for updates
    		Intent intent = new Intent(action);

    		Bundle bundle = new Bundle();
    		ArrayList<WikiArticle> arraylist = new ArrayList<WikiArticle>();
        	for (WikiArticle ar: array) 
        	{    		
        		arraylist.add(ar);
        	}
    			
        	bundle.putParcelableArrayList("wikiarticles", arraylist);
    		intent.putExtras(bundle);
    		
    		context.sendBroadcast(intent);
    	}
	  
	}
	
	
	
}

