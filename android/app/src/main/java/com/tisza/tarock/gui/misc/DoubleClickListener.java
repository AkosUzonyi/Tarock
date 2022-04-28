package com.tisza.tarock.gui.misc;

import android.content.*;
import android.os.*;
import android.view.*;
import androidx.preference.*;

public class DoubleClickListener implements View.OnClickListener
{
	private static final int doubleClickDelay = 400;

	private final boolean doubleClick;
	private final View.OnClickListener clickListener;

	private long lastClickTime = 0;

	public DoubleClickListener(Context context, View.OnClickListener clickListener)
	{
		this.clickListener = clickListener;
		doubleClick = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("double_click", false);
	}

	@Override
	public final void onClick(View view)
	{
		if (!doubleClick)
		{
			clickListener.onClick(view);
			return;
		}

		long time = SystemClock.elapsedRealtime();
		if (time < lastClickTime + doubleClickDelay)
			clickListener.onClick(view);
		lastClickTime = time;
	}
}
