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
	
	private final int[] audioResources;
	private final float frequency;

	private boolean enabled;

	public ZebiSound(Context context, float frequency, int ... audioResources)
	{
		this.context = context;
		handler = new Handler(context.getMainLooper());
		this.audioResources = audioResources;
		this.frequency = frequency;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	protected final void activate()
	{
		if (!enabled)
			return;

		if (rnd.nextFloat() >= frequency)
			return;

		int audioRes = audioResources[rnd.nextInt(audioResources.length)];
		MediaPlayer mediaPlayer = MediaPlayer.create(context, audioRes);
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener(MediaPlayer::release);
	}

	protected final void activateDelayed(int delaySec)
	{
		handler.postDelayed(activateRunnable, delaySec * 1000);
	}

	protected final void cancelActivation()
	{
		handler.removeCallbacks(activateRunnable);
	}
}
