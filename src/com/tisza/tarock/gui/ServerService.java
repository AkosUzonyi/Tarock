package com.tisza.tarock.gui;

import java.util.*;
	
import android.app.*;
import android.content.*;
import android.os.*;

import com.tisza.tarock.server.*;

public class ServerService extends Service
{
	private Server server;
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		int port = intent.getIntExtra("port", 8128);
		List<String> names = new ArrayList<String>();
		names.add(intent.getStringExtra("name0"));
		names.add(intent.getStringExtra("name1"));
		names.add(intent.getStringExtra("name2"));
		names.add(intent.getStringExtra("name3"));
		server = new Server(port, names);
		server.start();
		return Service.START_NOT_STICKY;
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		server.stop();
	}
	
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
