package com.tisza.tarock.server;

import java.util.concurrent.*;

public class GameThreadFactory implements ThreadFactory
{
	@Override
	public Thread newThread(Runnable r)
	{
		return new Thread(r, "GameThread");
	}
}
