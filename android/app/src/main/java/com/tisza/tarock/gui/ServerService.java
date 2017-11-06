package com.tisza.tarock.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.tisza.tarock.server.Server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
		File pointsDir = new File(getFilesDir(), "points");
		pointsDir.mkdir();
		server = new Server(port, names, pointsDir);
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
