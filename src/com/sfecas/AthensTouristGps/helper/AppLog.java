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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.sfecas.AthensTouristGps.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

/**
 * @author Sfecas Efstathios
 */
public class AppLog {

	private Context context;
	
	private String appDir;
	
	private static final int ERROR = 1;//;
	private static final int WARNING = 2;//;
	private static final int INFO = 3;//;
	private static final int DEBUG = 4;//;
	
	private String[] loggingLevels = new String[]{"no logging", "errors", "warning", "info", "debug"};
	
	/**
	 * Private constructor
	 */
	public AppLog(Context context) {
		
		this.context = context;
		// set application external storage folder
		appDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.APP_NAME;
	}
	
	private void log(int loggingLevel, String message) {
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (loggingLevel > Integer.parseInt(preferences.getString("logging_level", "0"))) {
			return;
		}
	
		String fileName = loggingLevels[loggingLevel]+"_"+DateFormat.format("yyyy-MM-dd", new Date()) + ".log";

		StringBuilder sb = new StringBuilder();
		sb.append(DateFormat.format("yyyy-MM-dd kk-mm-ss", new Date()));
		sb.append(" | ");
		sb.append(message);

		File logFile = new File(appDir + "/logs/" + fileName);

		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				return;
			}
		}

		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(sb.toString());
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			return;
		}
		
	}
	
	public void e(String message) {
		this.log(ERROR, message);
	}
	public void w(String message) {
		this.log(WARNING, message);
	}
	public void i(String message) {
		this.log(INFO, message);
	}
	public void d(String message) {
		this.log(DEBUG, message);
	}
	
}
