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
import android.content.Context;
import android.content.res.Configuration;

import com.sfecas.AthensTouristGps.Constants;

public class OrientationHelper {

	public float azimuth;
	public float pitch;
	public float roll;
	/**
	 * reverse landscape orientation workaround
	 */
	private int realOrientation;
	
	private Context context;
	
	public OrientationHelper(Context context) {
		
		this.context = context;

	}
	
	public void setOrientationValues(float azimuth, float pitch, float roll) {
		
		this.azimuth = azimuth;
		this.pitch = pitch;
		this.roll = roll;

		setRealOrientation(this.context.getResources().getConfiguration().orientation);
		
	}
	
	
	/**
	 * Returns compass rotation angle when orientation of the phone changes
	 */
	public int getOrientationAdjustment() {

		if (this.azimuth == 0 && this.pitch == 0 && this.roll == 0) {
			return 0;
		}

		switch (realOrientation) {

			case Constants.ORIENTATION_PORTRAIT:
				return 0;
			case Constants.ORIENTATION_LANDSCAPE:
				return 90;
			case Constants.ORIENTATION_REVERSE_LANDSCAPE:
				return -90;

		}

		return 0;
	}
	
	/**
	 * reverse landscape orientation workaround
	 * 
	 * @param orientation
	 */
	private void setRealOrientation(int orientation) {

		if (this.azimuth == 0 && this.pitch == 0 && this.roll == 0) {
			return;
		}		

		if (orientation != Configuration.ORIENTATION_PORTRAIT) {

			if (this.roll >= 25
					&& realOrientation != Constants.ORIENTATION_LANDSCAPE) {
				
				realOrientation = Constants.ORIENTATION_LANDSCAPE;
			}

			if (this.roll <= -25
					&& realOrientation != Constants.ORIENTATION_REVERSE_LANDSCAPE) {

				realOrientation = Constants.ORIENTATION_REVERSE_LANDSCAPE;
			}

		} else {
			realOrientation = Constants.ORIENTATION_PORTRAIT;
		}

	}
	
}
