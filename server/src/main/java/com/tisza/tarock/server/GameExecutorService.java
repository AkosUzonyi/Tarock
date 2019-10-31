package com.tisza.tarock.server;

import java.util.concurrent.*;

public class GameExecutorService extends ScheduledThreadPoolExecutor
{
	public GameExecutorService()
	{
		super(1, new GameThreadFactory());
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t)
	{
		super.afterExecute(r, t);
		if (t == null && r instanceof Future<?>)
		{
			try
			{
				Future<?> future = (Future<?>)r;
				if (future.isDone())
				{
					future.get();
				}
			}
			catch (CancellationException ce)
			{
				t = ce;
			}
			catch (ExecutionException ee)
			{
				t = ee.getCause();
			}
			catch (InterruptedException ie)
			{
				Thread.currentThread().interrupt();
			}
		}
		if (t != null)
		{
			t.printStackTrace();
		}
	}

	private static class GameThreadFactory implements ThreadFactory
	{
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "GameThread");
		}
	}
}
