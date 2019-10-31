package com.tisza.tarock;

import com.tisza.tarock.server.*;

import java.io.*;
import java.util.concurrent.*;

public class Main
{
	public static final File STATIC_DIR = new File("/usr/share/tarock");
	public static final File DATA_DIR = new File("/var/lib/tarock");
	public static final ScheduledExecutorService GAME_EXECUTOR_SERVICE = new GameExecutorService();

	public static void main(String[] args)
	{
		Server server = new Server(8128);
		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			GAME_EXECUTOR_SERVICE.shutdownNow();
			server.stop();
			try
			{
				GAME_EXECUTOR_SERVICE.awaitTermination(1, TimeUnit.SECONDS);
				server.awaitTermination(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}));
	}
}
