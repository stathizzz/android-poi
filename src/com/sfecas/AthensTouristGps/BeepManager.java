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
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import com.sfecas.AthensTouristGps.service.AppService;
import com.sfecas.AthensTouristGps.service.AppServiceConnection;
import com.sfecas.AthensTouristGps.R;

/**
 * @author Sfecas Efstathios
 * Manages beeps and vibrations for {@link CaptureActivity}.
 */
public final class BeepManager
{

	private static final String TAG = BeepManager.class.getSimpleName();

	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;

	private App app;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private boolean vibrate;

	// AppServiceConnection serviceConnection;

	public BeepManager(App app)
	{
		this.app = app;
		this.mediaPlayer = null;
		updatePrefs();

		// serviceConnection = new AppServiceConnection(activity,
		// appServiceConnectionCallback);

	}

	void updatePrefs()
	{
		playBeep = shouldBeep(app);
		vibrate = !playBeep;
		if (playBeep && mediaPlayer == null)
		{
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			// activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

			mediaPlayer = buildMediaPlayer(app);
		}
	}

	public void playBeepSoundOrVibrate()
	{
		if (playBeep && mediaPlayer != null)
		{
			mediaPlayer.start();
		}
		if (vibrate)
		{
			Vibrator vibrator = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private static boolean shouldBeep(App app)
	{
		boolean shouldPlayBeep = Boolean.parseBoolean(app.getPreferences().getString("signal_method_units", "true"));
		if (shouldPlayBeep)
		{
			// See if sound settings overrides this
			AudioManager audioService = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
			if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
			{
				shouldPlayBeep = false;
			}
		}
		return shouldPlayBeep;
	}

	private static MediaPlayer buildMediaPlayer(App app)
	{
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// When the beep has finished playing, rewind to queue up another one.
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			public void onCompletion(MediaPlayer player)
			{
				player.seekTo(0);
			}
		});

		AssetFileDescriptor file = app.getResources().openRawResourceFd(R.raw.beep);
		try
		{
			mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
			file.close();
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
		}
		catch (IOException ioe)
		{
			Log.w(TAG, ioe);
			mediaPlayer = null;
		}
		return mediaPlayer;
	}

	public void release()
	{
		if (this.mediaPlayer != null)
		{
			this.mediaPlayer.stop();
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
	}
}
