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
				while (!closeRequested)
				{
					Packet p = Packet.readPacket(is);
					for (PacketHandler ph : packetHandlers)
					{
						ph.handlePacket(p);
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
				while (!closeRequested || !packetsToSend.isEmpty())
				{
					try
					{
						Packet p = packetsToSend.take();
						p.writePacket(os);
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
	
	public Connection(Socket s) throws IOException
	{
		socket = s;
		is = socket.getInputStream();
		os = socket.getOutputStream();
		start();
	}
	
	public void start()
	{
		if (!closeRequested)
		{
			readerThread.start();
			writerThread.start();
		}
	}
	
	public void addPacketHandler(PacketHandler ph)
	{
		List<PacketHandler> newPacketHandlers = new ArrayList<PacketHandler>(packetHandlers);
		newPacketHandlers.add(ph);
		packetHandlers = newPacketHandlers;
	}
	
	public void removePacketHandler(PacketHandler ph)
	{
		List<PacketHandler> newPacketHandlers = new ArrayList<PacketHandler>(packetHandlers);
		newPacketHandlers.remove(ph);
		packetHandlers = newPacketHandlers;
	}
	
	public void sendPacket(Packet p)
	{
		if (isAlive())
		{
			packetsToSend.offer(p);
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
			for (PacketHandler ph : packetHandlers)
			{
				ph.connectionClosed();
			}
			packetHandlers = null;
		}
	}
}
