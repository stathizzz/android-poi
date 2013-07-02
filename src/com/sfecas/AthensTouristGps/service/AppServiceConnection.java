package com.sfecas.AthensTouristGps.service;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sfecas.AthensTouristGps.Constants;

public class AppServiceConnection {

	private Context context;

	/**
	 * GPS service connection
	 */
	private AppService appService;
	
	private Runnable runnable;

	/**
	 * ServiceConnection object
	 */
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {

			Log.d(Constants.TAG, "onServiceConnected " + this.toString());
			appService = ((AppService.LocalBinder) service).getService();
			// executing activity's callback 
			new Handler().post(runnable);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(Constants.TAG, "onServiceDisconnected");
		}

	};
	
	public AppServiceConnection(Context c, Runnable r) {		
		this.context = c; 		
		this.runnable = r;
	}
	
	/**
	 * binding from the application service
	 */
	public void bindAppService() {
		
		Log.v(Constants.TAG, "bindAppService");
		
		Intent i = new Intent(context, AppService.class);
		if (!context.bindService(i, serviceConnection, 0)) {
			Toast.makeText(context, "Can't connect to GPS service", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * unbinding from the application service
	 */
	public void unbindAppService() {

		Log.v(Constants.TAG, "AppServiceConnection: unbindAppService");

		// detach our existing connection
		context.unbindService(serviceConnection);
		appService = null;
	}
	
	/**
	 * Service getter
	 */
	public AppService getService() {
		
		return appService;
	}
	
	/**
	 * start application service
	 */
	public void startService() {
		
		Intent i = new Intent(context, AppService.class);
		context.startService(i);	
	}
	
	/**
	 * stop application service
	 */
	public void stopService() {

		Intent i = new Intent(context, AppService.class);
		context.stopService(i);
	}

	
}
