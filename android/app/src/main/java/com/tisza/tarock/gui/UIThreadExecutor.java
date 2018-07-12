package com.tisza.tarock.gui;

import android.os.*;

import java.util.concurrent.*;

public class UIThreadExecutor implements Executor
{
	private final Handler handler = new Handler(Looper.getMainLooper());

	@Override
	public void execute(Runnable command)
	{
		handler.post(command);
	}
}
