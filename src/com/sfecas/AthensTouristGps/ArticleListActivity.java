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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.AdapterContextMenuInfo;

import com.sfecas.AthensTouristGps.helper.POI;
import com.sfecas.AthensTouristGps.helper.Utils;
import com.sfecas.AthensTouristGps.map.MapActivityV1;
import com.sfecas.AthensTouristGps.map.PoiMapActivity;
import com.sfecas.AthensTouristGps.service.*;
import com.sfecas.AthensTouristGps.view.CompassImage;
import com.sfecas.AthensTouristGps.webservice.ArticleUtilizer;
import com.sfecas.AthensTouristGps.webservice.WikiArticle;
import com.sfecas.AthensTouristGps.webservice.WikiLocator;
import com.sfecas.AthensTouristGps.webservice.WikiUtilizer;
import com.sfecas.AthensTouristGps.webservice.ArticleUtilizer.Lat;
import com.sfecas.AthensTouristGps.webservice.ArticleUtilizer.Limit;
import com.sfecas.AthensTouristGps.webservice.ArticleUtilizer.Lng;
import com.sfecas.AthensTouristGps.webservice.ArticleUtilizer.Radius;
import com.sfecas.AthensTouristGps.webservice.ArticleUtilizer.Type;
import com.sfecas.AthensTouristGps.webservice.WikiUtilizer.*;
import com.sfecas.AthensTouristGps.R;
import java.util.*;
/**
 * @author Sfecas Efstathios
 * Wiki list activity
 */
public class ArticleListActivity extends ListActivity
{

	/**
	 * Reference to app object
	 */
	private App app;

	private String importWikisFileName;
	private String distanceUnit;
	private WikiArrayAdapter wikiAdapter;

	private ArrayList<WikiArticle> wikis;

	private Location currentLocation;

	/**
	 * azimuth (received from orientation sensor)
	 */
	private float azimuth = 0;

	/**
	 * Service connection object
	 */
	private AppServiceConnection serviceConnection;

	/**
	 * Article updates broadcast receiver
	 */
	protected BroadcastReceiver wikiBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.d(Constants.TAG, "ArticleListActivity: NETWORK BROADCAST MESSAGE RECEIVED");

			Bundle bundle = intent.getExtras();
						
			wikis = bundle.getParcelableArrayList("wikiarticles");

			if (wikis.size() > 0)
			{
				wikiAdapter.clear(); 
				wikiAdapter.addAll(wikis);	
				wikiAdapter.sortByWikiDistance();
				wikiAdapter.notifyDataSetChanged();
			}			
		}
	};

	protected class WikiArrayAdapter extends BaseWikiArrayAdapter
	{
		Bitmap arrowBitmap;
		BitmapDrawable bmd;

		public WikiArrayAdapter(Context context, int textViewResourceId, Location currentLocation,
				ArrayList<WikiArticle> items)
		{
			super(context, textViewResourceId, currentLocation, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;

			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.wiki_list_item, null);
			}
			String distStr = "";		

			WikiArticle wp = this.getItems().get(position);
			if (wp != null)
			{
				if (currentLocation != null)
				{
					//TODO
				}

				TextView wikiTitle = (TextView) v.findViewById(R.id.wiki_title);
				TextView wikiDetails = (TextView) v.findViewById(R.id.wiki_details);
				TextView wikiDistance = (TextView) v.findViewById(R.id.wiki_distance);

				// Set value for the first text field
				if (wikiTitle != null)
				{
					wikiTitle.setText(Utils.shortenStr(wp.getTitle().toString(), 32));
				}

				NumberFormat nf = NumberFormat.getInstance(java.util.Locale.ENGLISH);
				Number myNumber;
				try
				{
					myNumber = nf.parse(wp.getDistance());
					
					float dist = myNumber.floatValue();
										
					distStr = Utils.formatDistance(dist, distanceUnit)
							+ " "
							+ Utils.getLocalizedDistanceUnit(ArticleListActivity.this, dist, distanceUnit);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				
				}
			
				// set value for the second text field
				if (wikiDetails != null)
				{
					wikiDetails.setText(wp.getUrl()
							+ "|"
							+ wp.getLatitude()
							+ "|"
							+ wp.getLongitude()
							+ "|"
							+ distStr);
				}

				if (wikiDistance != null)
				{
					wikiDistance.setText(distStr);
				}
			}
			
			return v;
		}
	}

	/**
	 * Called when the activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		serviceConnection = new AppServiceConnection(this, appServiceConnectionCallback);
		
		// initializing with last known location, so we can calculate distance to Wikis
		app = (App) getApplication();
		currentLocation = app.getCurrentLocation();

		//AppService appService = serviceConnection.getService(); 
		
		if (currentLocation == null)
		{
			Toast.makeText(ArticleListActivity.this, R.string.no_current_poi, Toast.LENGTH_LONG).show();
			
			Intent intent = new Intent(this, ThirdMainActivity.class);
			startActivity(intent);
			return;
		}

		registerForContextMenu(this.getListView());
		
		wikis = new ArrayList<WikiArticle>();
		
		wikiAdapter = new WikiArrayAdapter(this, R.layout.wiki_list_item, currentLocation, wikis);
		
		setListAdapter(wikiAdapter);
		
		ArticleUtilizer articleMgr = new ArticleUtilizer();
		
		//create the article data
		ArticleUtilizer.Lat _lat = articleMgr.new Lat(currentLocation.getLatitude());
		ArticleUtilizer.Lng _lng = articleMgr.new Lng(currentLocation.getLongitude());
		WikiUtilizer.Format _frm = articleMgr.new Format("json");
		/* grab the first 20 results from the web */
		ArticleUtilizer.Limit _lmt= articleMgr.new Limit(20);
		/* search in a range of 100km */
		ArticleUtilizer.Radius _rd = articleMgr.new Radius(100000);
		
		/* currently we are only interested in landmark locations */
		ArticleUtilizer.Type _type = articleMgr.new Type(ArticleUtilizer.ArticleType.landmark);
		
		String url = articleMgr.FormatUrl(_frm, _lat, _lng, _type, _lmt, _rd);
		WikiLocator wikiLocator = WikiLocator.getInstance(this);
		wikiLocator.DoRequest(url);
	}

	private Runnable appServiceConnectionCallback = new Runnable()
	{

		@Override
		public void run()
		{

			AppService appService = serviceConnection.getService();

			if (appService == null)
			{
				Toast.makeText(ArticleListActivity.this, R.string.gps_service_not_connected, Toast.LENGTH_SHORT).show();
				return;
			}

			if (!appService.isOnline())
			{
				Toast.makeText(ArticleListActivity.this, R.string.internet_off, Toast.LENGTH_LONG).show();
				
				Intent intent = new Intent(ArticleListActivity.this, ThirdMainActivity.class);
				startActivity(intent);
				appService.setNetworkInUse(false);
				return;
			}
			
			// this activity is started by MainActivity which is always
			// listening for network updates
			appService.setNetworkInUse(true);	
		}
	};

	/**
	 * Edit Wiki
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		final WikiArticle wiki = wikiAdapter.getItem(position);
		this.readArticle(wiki);
	}

	/**
	 * 
	 */
	@Override
	public void onPause()
	{
		unregisterReceiver(wikiBroadcastReceiver);
		// unbind AppService
		//serviceConnection.unbindAppService();
		super.onPause();
	}

	/**
	 * 
	 */
	@Override
	protected void onDestroy()
	{
		if (wikis != null)
		{
			wikis = null;
		}

		serviceConnection = null;
		app = null;
		super.onDestroy();
	}

	/**
	 * onResume event handler
	 */
	@Override
	protected void onResume()
	{

		super.onResume();

		// set locale to greek to avoid mantling with geo point commas
		Utils.setLocale(this, "en");
		
		this.initializeMeasuringUnits();
		
		// registering receiver for location updates
		registerReceiver(wikiBroadcastReceiver, new IntentFilter(Constants.ACTION_NETWORK_UPDATES));

		// bind to AppService, appServiceConnectionCallback will be called once bound
		serviceConnection.bindAppService();
	}

	/**
	 * initialize measuring units with up to date values
	 */
	private void initializeMeasuringUnits()
	{
		distanceUnit = app.getPreferences().getString("distance_units", "km");
	}
	/**
	 * onCreateOptionsMenu handler
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poi_menu, menu);
		return true;
	}

	/**
     * 
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int itemId = item.getItemId();
		if (itemId == R.id.deleteMenuItem) {
			// clear all Wikis with confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.are_you_sure).setCancelable(true).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// delete all Wikis
					wikiAdapter.clear();
					wikiAdapter.notifyDataSetChanged();
					Toast.makeText(ArticleListActivity.this, R.string.all_articles_deleted, Toast.LENGTH_SHORT).show();

				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		} else if (itemId == R.id.importMenuItem) {
			this.importFromXMLFile();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * Create context menu for selected item
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{

		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle(getString(R.string.articles));
		menu.add(Menu.NONE, 0, 0, R.string.show);
		menu.add(Menu.NONE, 1, 1, R.string.delete);
		menu.add(Menu.NONE, 2, 2, R.string.email_to);
		menu.add(Menu.NONE, 3, 3, R.string.show_on_map);
		menu.add(Menu.NONE, 4, 4, R.string.navigate);
	}

	/**
	 * Handle activity menu
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		final WikiArticle wiki = wikiAdapter.getItem((int)info.id);//((int) info.id);
		
		switch (item.getItemId())
		{

		case 0:
			openUrl(wiki.getUrl());
			return true;
		case 1:
			// delete one Wiki with confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure?").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//wikis = Utils.updateWikisArray(app);
					//Wikiarticle article = 
					wikiAdapter.remove(wiki);
					wikiAdapter.notifyDataSetChanged();

					Toast.makeText(ArticleListActivity.this, R.string.poi_deleted, Toast.LENGTH_SHORT).show();
				}
			}).setNegativeButton("No", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		case 2:
			// email Wiki data using default email client

			String messageBody = getString(R.string.title) + ": "
					+ wiki.getTitle() + "\n\n" + getString(R.string.lat) + ": "
					+ wiki.getLatitude() + "\n" + getString(R.string.lng)
					+ ": " + wiki.getLongitude() + "\n\n"
					+ "http://maps.google.com/?ll=" + wiki.getLatitude() + "," + wiki.getLongitude()
					+ "&z=10" + "\n\n" + wiki.getUrl();
			
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject_poi));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, messageBody);
			this.startActivity(Intent.createChooser(emailIntent, getString(R.string.sending_email)));
			return true;
		case 3:
			// showing Wiki on the Google map
			showOnMap(wiki);
			
			return true;
		case 4:
			double lat = Double.parseDouble(wiki.getLatitude());
			double lng = Double.parseDouble(wiki.getLongitude());
			
			POI poi = new POI(wiki.getTitle(), wiki.getId(), lat, lng, 0, wiki.getUrl(), null, null);
			
			app.setCurrentPOI(poi);

			Intent intent = new Intent(this, ThirdMainActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	
	protected void showOnMap(WikiArticle wiki)
	{
		Intent i = new Intent(this, PoiMapActivity.class);

		// using Bundle to pass track id into new activity
		Bundle b = new Bundle();
		b.putInt("mode", Constants.SHOW_POI);
		b.putDouble("latitude", Double.parseDouble(wiki.getLatitude()));
		b.putDouble("longitude", Double.parseDouble(wiki.getLongitude()));
		i.putExtras(b);
		
		startActivity(i);
	}
	
	protected void readArticle(WikiArticle article)
	{
		openUrl(article.getUrl());
	}


	/**
	 * Imports Wikis from gpx file
	 */
	protected void importFromXMLFile()
	{

		File importFolder = new File(app.getAppDir() + "/pois");

		final String importFiles[] = importFolder.list();

		if (importFiles == null || importFiles.length == 0)
		{
			Toast.makeText(ArticleListActivity.this, "Import folder is empty", Toast.LENGTH_SHORT).show();
			return;
		}

		// 1st file is selected by default
		importWikisFileName = importFiles[0];

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setSingleChoiceItems(importFiles, 0, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				importWikisFileName = importFiles[whichButton];
			}
		});

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				try
				{
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					File file = new File(app.getAppDir() + "/articles", importWikisFileName);

					Document doc = db.parse(file);
					doc.getDocumentElement().normalize();

					NodeList wikisList = doc.getElementsByTagName("Wiki");

					boolean updateRequired = false;

					for (int i = 0; i < wikisList.getLength(); i++)
					{
						double latE6 = (Double.parseDouble(((Element) wikisList.item(i)).getAttribute("latitude")) * 1E6);
						double lngE6 = (Double.parseDouble(((Element) wikisList.item(i)).getAttribute("longitude")) * 1E6);
						String title = "";
						String ids = "";
						String url = "";
						String dist = "";
						int activity = 0;
						long time = 0;

						Node item = wikisList.item(i);

						NodeList properties = item.getChildNodes();
						for (int j = 0; j < properties.getLength(); j++)
						{
							Node property = properties.item(j);
							String name = property.getNodeName();

							if (name.equalsIgnoreCase("activity")
									&& property.getFirstChild() != null)
							{
								activity = Integer.parseInt(property.getFirstChild().getNodeValue());
							}
							if (name.equalsIgnoreCase("time")
									&& property.getFirstChild() != null)
							{
								time = (new SimpleDateFormat("yyyy-MM-dd H:mm:ss")).parse(property.getFirstChild().getNodeValue()).getTime();
							}
							if (name.equalsIgnoreCase("title")
									&& property.getFirstChild() != null)
							{
								title = property.getFirstChild().getNodeValue();
							}
							if (name.equalsIgnoreCase("distance")
									&& property.getFirstChild() != null)
							{
								dist = property.getFirstChild().getNodeValue();
							}
							if (name.equalsIgnoreCase("id")
									&& property.getFirstChild() != null)
							{
								ids = property.getFirstChild().getNodeValue();
							}
							if (name.equalsIgnoreCase("url")
									&& property.getFirstChild() != null)
							{
								url = property.getFirstChild().getNodeValue();
							}							
						}
						WikiArticle article = new WikiArticle();
						article.setLatitude(String.valueOf(latE6));
						article.setLongitude(String.valueOf(lngE6));
						article.setId(ids);
						article.setTitle(title);
						article.setDistance(dist);
						article.setUrl(url);
						wikiAdapter.add(article);
					}

					if (updateRequired)
					{
						wikiAdapter.clear();
											
						wikiAdapter.notifyDataSetChanged();
					}

					Toast.makeText(ArticleListActivity.this, R.string.import_completed, Toast.LENGTH_SHORT).show();

				}
				catch (IOException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}
				catch (ParserConfigurationException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}
				catch (ParseException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}
				catch (SAXException e)
				{
					Log.v(Constants.TAG, e.getMessage());
				}

				dialog.dismiss();
			}
		}).setTitle(R.string.select_file).setCancelable(true);

		AlertDialog alert = builder.create();

		alert.show();
	}

	protected void openUrl(String url)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

}
