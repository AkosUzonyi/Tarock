package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

public abstract class Packet
{
	private static final Map<Integer, Class<? extends Packet>> idToPacket = new HashMap<Integer, Class<? extends Packet>>();
	private static final Map<Class<? extends Packet>, Integer> packetToID = new HashMap<Class<? extends Packet>, Integer>();
	
	private static void register(int id, Class<? extends Packet> cls)
	{
		if (idToPacket.containsKey(id) || packetToID.containsKey(cls))
			throw new IllegalArgumentException();
		idToPacket.put(id, cls);
		packetToID.put(cls, id);
	}
	
	public int getID()
	{
		return packetToID.get(getClass());
	}

	public static Packet readPacket(InputStream is) throws IOException
	{
		DataInputStream dis = new DataInputStream(is);
		Class<? extends Packet> cls = idToPacket.get(dis.readByte());
		Packet p;
		try
		{
			p = cls.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		p.readData(dis);
		return p;
	}
	
	public void writePacket(OutputStream os) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeByte(getID());
		writeData(dos);
	}
	
	protected abstract void readData(DataInputStream dis) throws IOException;
	protected abstract void writeData(DataOutputStream dos) throws IOException;
	
	static
	{
		
	}
}
