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
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import android.preference.Preference.OnPreferenceChangeListener;

import com.sfecas.AthensTouristGps.helper.ArrayUtils;
import com.sfecas.AthensTouristGps.R;


/**
 * @author Sfecas Efstathios
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	protected App app;

	/**
	 * Called when the activity created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		// reference to application object
		app = ((App) getApplicationContext());

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);

		onSharedPreferenceChanged(preferences, "distance_units");
		onSharedPreferenceChanged(preferences, "speed_units");
		onSharedPreferenceChanged(preferences, "elevation_units");
		onSharedPreferenceChanged(preferences, "signal_method_units");
		onSharedPreferenceChanged(preferences, "coord_units");
		onSharedPreferenceChanged(preferences, "true_north");
		
		// setting listener for distance units changes
		ListPreference distanceUnitsPreference = (ListPreference) findPreference("distance_units");
		distanceUnitsPreference.setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						
						return true;
					}
				});

	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		Preference pref = findPreference(key);

		//----------------------------------------------------------------------
		// validate user input 
		if (key.equals("segment_custom_1") || key.equals("segment_custom_2")) {

			if (pref instanceof EditTextPreference) {

				EditTextPreference textPref = (EditTextPreference) pref;

				String[] tmpArr = textPref.getText().split(",");

				for (int i = 0; i < tmpArr.length; i++) {

					try {

						// check if values entered are in ascending order and unique
						if (tmpArr.length > 1 && i < tmpArr.length - 1) {
							if (Double.parseDouble(tmpArr[i]) >= Double.parseDouble(tmpArr[i + 1])) {
								textPref.setText("5,10,15,20");
								return;
							}
						} else {
							// check number format only
							Double.parseDouble(tmpArr[i]);
						}

					} catch (NumberFormatException e) {
					
						textPref.setText("5,10,15,20");
						return;
					}
				}

			}
		}
		//----------------------------------------------------------------------

		if (pref instanceof ListPreference) {

			String[] prefKeys = { "speed_units", "distance_units", "elevation_units", "signal_method_units", "coord_units", "segmenting_mode" };
			// show set values only for defined keys 
			if (!ArrayUtils.contains(prefKeys, key)) {
				return;
			}

			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());

		}

	}

}