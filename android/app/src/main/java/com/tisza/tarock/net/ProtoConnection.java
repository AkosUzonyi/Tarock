package com.tisza.tarock.net;

import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.proto.MainProto.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ProtoConnection
{
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private List<MessageHandler> packetHandlers = new ArrayList<>();
	private BlockingQueue<Message> messagesToSend = new LinkedBlockingQueue<>();
	private boolean closeRequested = false;
	
	private Thread readerThread = new Thread(new Runnable()
	{
		public void run()
		{
			try
			{
				while (!closeRequested)
				{
					MainProto.Message message = Message.parseDelimitedFrom(is);
					synchronized (packetHandlers)
					{
						for (MessageHandler handler : packetHandlers)
						{
							handler.handleMessage(message);
						}
					}
				}
			}
			catch (EOFException e) {}
			catch (SocketException e) {}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				closeRequest();
			}
		}
	});
	
	private Thread writerThread = new Thread(new Runnable()
	{
		public void run()
		{
			try
			{
				while (!closeRequested || !messagesToSend.isEmpty())
				{
					try
					{
						Message message = messagesToSend.take();
						message.writeDelimitedTo(os);
					}
					catch (InterruptedException e) {}
				}
			}
			catch (SocketException e) {}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				closeRequest();
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
	});
	
	public ProtoConnection(Socket s) throws IOException
	{
		socket = s;
		socket.setTcpNoDelay(true);
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}
	
	public void start()
	{
		if (!closeRequested)
		{
			readerThread.start();
			writerThread.start();
		}
	}
	
	public void addMessageHandler(MessageHandler handler)
	{
		synchronized (packetHandlers)
		{
			packetHandlers.add(handler);
		}
	}
	
	public void removeMessageHandler(MessageHandler handler)
	{
		synchronized (packetHandlers)
		{
			packetHandlers.remove(handler);
		}
	}
	
	public void sendMessage(Message message)
	{
		if (isAlive())
		{
			messagesToSend.offer(message);
		}
	}
	
	public void closeRequest()
	{
		if (!closeRequested)
		{
			closeRequested = true;
			writerThread.interrupt();
		}
	}
	
	public boolean isAlive()
	{
		return !closeRequested;
	}
	
	private void close() throws IOException
	{
		closeRequested = true;
		if (socket != null)
		{
			socket.close();
			socket = null;
			synchronized (packetHandlers)
			{
				for (MessageHandler handler : packetHandlers)
				{
					handler.connectionClosed();
				}
			}
			packetHandlers = null;
		}
	}
}
