package com.tisza.tarock.zebisound;

import android.content.*;
import android.media.*;
import android.os.*;
import com.tisza.tarock.message.*;

import java.util.*;

public abstract class ZebiSound implements EventHandler
{
	private final Context context;
	private final Handler handler;
	protected final Random rnd = new Random();
	private final Runnable activateRunnable = this::activate;

	public ZebiSound(Context context)
	{
		this.context = context;
		handler = new Handler(context.getMainLooper());
	}

	protected abstract int getAudioRes();
	protected abstract float getFrequency();

	protected final void activate()
	{
		if (rnd.nextFloat() >= getFrequency())
			return;

		MediaPlayer mediaPlayer = MediaPlayer.create(context, getAudioRes());
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener(MediaPlayer::release);
	}

	protected final void activateDelayed(int delayMillis)
	{
		handler.postDelayed(activateRunnable, delayMillis);
	}

	protected final void cancelActivation()
	{
		handler.removeCallbacks(activateRunnable);
	}
}
