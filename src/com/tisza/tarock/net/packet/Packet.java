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
		int id = dis.readByte();
		Class<? extends Packet> cls = idToPacket.get(id);
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
		dos.flush();
	}
	
	protected abstract void readData(DataInputStream dis) throws IOException;
	protected abstract void writeData(DataOutputStream dos) throws IOException;
	
	static
	{
		register(0, PacketLogin.class);
		register(1, PacketLoginFailed.class);
		register(2, PacketStartGame.class);
		register(3, PacketPlayerCards.class);
		register(4, PacketTurn.class);
		register(5, PacketThrowCards.class);
		register(6, PacketCardsThrown.class);
		register(7, PacketAvailableBids.class);
		register(8, PacketBid.class);
		register(9, PacketChange.class);
		register(10, PacketChangeDone.class);
		register(11, PacketAvailableCalls.class);
		register(12, PacketCall.class);
		register(13, PacketAvailabeContras.class);
		register(14, PacketAnnounce.class);
		register(15, PacketContra.class);
		register(16, PacketPlayCard.class);
		register(17, PacketAnnouncementStatistics.class);
		register(18, PacketPoints.class);
		register(19, PacketReadyForNewGame.class);
		
		register(20, PacketMessage.class);
		register(21, PacketSkartTarock.class);
	}
}
