package com.tisza.tarock.net;

import com.tisza.tarock.proto.MainProto.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ProtoConnection implements Closeable
{
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private final Object packetHandlersLock = new Object();
	private List<MessageHandler> packetHandlers = new ArrayList<>();
	private BlockingQueue<Message> messagesToSend = new LinkedBlockingQueue<>();
	private volatile boolean started = false;
	private volatile boolean closeRequested = false;

	private Thread readerThread = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				while (!closeRequested)
				{
					Message message = Message.parseDelimitedFrom(is);

					if (message == null)
						break;

					synchronized (packetHandlersLock)
					{
						for (MessageHandler handler : packetHandlers)
						{
							handler.handleMessage(message);
						}
					}
				}
			}
			catch (EOFException e) {}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (!closeRequested)
				{
					try
					{
						close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	});
	
	private Thread writerThread = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				while (!closeRequested)
				{
					try
					{
						Message message = messagesToSend.take();
						message.writeDelimitedTo(os);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (!closeRequested)
				{
					try
					{
						close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	});
	
	public ProtoConnection(Socket s) throws IOException
	{
		socket = s;
		socket.setTcpNoDelay(true);
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}
	
	public synchronized void start()
	{
		if (started || closeRequested)
			throw new IllegalStateException();

		started = true;
		readerThread.start();
		writerThread.start();
	}
	
	public void addMessageHandler(MessageHandler handler)
	{
		synchronized (packetHandlersLock)
		{
			packetHandlers.add(handler);
		}
	}
	
	public void removeMessageHandler(MessageHandler handler)
	{
		synchronized (packetHandlersLock)
		{
			packetHandlers.remove(handler);
		}
	}
	
	public void sendMessage(Message message)
	{
		if (!isAlive())
			throw new IllegalStateException();

		messagesToSend.offer(message);
	}

	private void stopThreads()
	{
		stopThread(readerThread);
		stopThread(writerThread);
	}

	private void stopThread(Thread thread)
	{
		if (!thread.isAlive())
			return;

		if (thread == Thread.currentThread())
			return;

		thread.interrupt();
		try
		{
			thread.join(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		if (thread.isAlive())
		{
			System.err.println("could not stop thread: " + thread);
		}
	}

	public synchronized boolean isAlive()
	{
		return started && !closeRequested;
	}
	
	@Override
	public synchronized void close() throws IOException
	{
		if (closeRequested)
			return;

		closeRequested = true;

		if (socket != null)
		{
			socket.close();
			socket = null;
		}

		stopThreads();

		synchronized (packetHandlersLock)
		{
			for (MessageHandler handler : packetHandlers)
			{
				handler.connectionClosed();
			}
			packetHandlers = null;
		}
	}
}
