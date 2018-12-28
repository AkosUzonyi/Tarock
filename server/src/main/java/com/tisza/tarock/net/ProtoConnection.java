package com.tisza.tarock.net;

import com.tisza.tarock.proto.*;
import com.tisza.tarock.proto.MainProto.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

public class ProtoConnection implements Closeable
{
	private static final String HELLO_STRING = "Tarokk";
	private static final int VERSION = 1;
	private static final int KEEP_ALIVE_DELAY = 8;
	private static final int SOCKET_TIMEOUT = 10;

	private final Executor messageHandlerExecutor;
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

					if (message.getMessageTypeCase() == Message.MessageTypeCase.KEEPALIVE)
						continue;

					synchronized (packetHandlersLock)
					{
						for (MessageHandler handler : packetHandlers)
						{
							messageHandlerExecutor.execute(() -> handler.handleMessage(message));
						}
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
	
	private Thread writerThread = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				ByteBuffer helloByteBuffer = ByteBuffer.allocate(10);
				helloByteBuffer.put(HELLO_STRING.getBytes(StandardCharsets.UTF_8), 0, 6);
				helloByteBuffer.putInt(6, VERSION);
				os.write(helloByteBuffer.array());

				while (!closeRequested)
				{
					try
					{
						Message message = messagesToSend.poll(KEEP_ALIVE_DELAY, TimeUnit.SECONDS);

						if (message == null)
							message = MainProto.Message.newBuilder().setKeepAlive(MainProto.KeepAlive.getDefaultInstance()).build();

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
	
	public ProtoConnection(Socket socket, Executor messageHandlerExecutor) throws IOException
	{
		this.socket = socket;
		this.messageHandlerExecutor = messageHandlerExecutor;

		socket.setTcpNoDelay(true);
		socket.setSoTimeout(SOCKET_TIMEOUT * 1000);

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
				messageHandlerExecutor.execute(() -> handler.connectionClosed());
			}
			packetHandlers = null;
		}
	}
}
