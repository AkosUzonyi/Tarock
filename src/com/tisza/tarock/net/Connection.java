package com.tisza.tarock.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import com.tisza.tarock.net.packet.*;

public class Connection
{
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private List<PacketHandler> packetHandlers = new ArrayList<PacketHandler>();
	private BlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<Packet>();
	private boolean closeRequested = false;
	
	private Thread readerThread = new Thread(new Runnable()
	{
		public void run()
		{
			try
			{
				while (!Thread.interrupted())
				{
					Packet p = Packet.readPacket(is);
					for (PacketHandler ph : packetHandlers)
					{
						ph.handlePacket(p);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				try
				{
					close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	});
	
	private Thread writerThread = new Thread(new Runnable()
	{
		public void run()
		{
			try
			{
				while (!closeRequested || !packetsToSend.isEmpty())
				{
					Packet p = packetsToSend.take();
					p.writePacket(os);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					os.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	});
	
	public Connection(Socket s) throws IOException
	{
		socket = s;
		is = socket.getInputStream();
		os = socket.getOutputStream();
		readerThread.start();
		writerThread.start();
	}
	
	public void addPacketHandler(PacketHandler ph)
	{
		packetHandlers.add(ph);
	}
	
	public void removePacketHandler(PacketHandler ph)
	{
		packetHandlers.remove(ph);
	}
	
	public void sendPacket(Packet p)
	{
		packetsToSend.offer(p);
	}
	
	public void closeRequest()
	{
		closeRequested = true;
	}
	
	@SuppressWarnings("deprecation")
	private void close() throws IOException
	{
		readerThread.stop();
		writerThread.stop();
		is.close();
		os.close();
		socket.close();
		socket = null;
		for (PacketHandler ph : packetHandlers)
		{
			ph.connectionClosed();
		}
	}
}
