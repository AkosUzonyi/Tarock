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
		register(2, PacketServerStatus.class);
		register(3, PacketStartGame.class);
		register(4, PacketPlayerCards.class);
		register(5, PacketTurn.class);
		register(6, PacketThrowCards.class);
		register(7, PacketCardsThrown.class);
		register(8, PacketAvailableBids.class);
		register(9, PacketBid.class);
		register(10, PacketChange.class);
		register(11, PacketChangeDone.class);
		register(12, PacketAvailableCalls.class);
		register(13, PacketCall.class);
		register(14, PacketAvailabeContras.class);
		register(15, PacketAnnounce.class);
		register(16, PacketContra.class);
		register(17, PacketPlayCard.class);
		register(18, PacketAnnouncementStatistics.class);
		register(19, PacketPoints.class);
		register(20, PacketReadyForNewGame.class);
		
		register(21, PacketMessage.class);
		register(22, PacketSkartTarock.class);
	}
}
