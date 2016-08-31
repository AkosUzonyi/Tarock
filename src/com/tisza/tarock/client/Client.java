package com.tisza.tarock.client;

import java.net.*;
import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class Client implements PacketHandler
{
	private Connection conncection;
	int player = -10;
	
	public Client(String host, int port, String name) throws Exception
	{
		conncection = new Connection(new Socket(host, port));
		conncection.sendPacket(new PacketLogin(name));
		conncection.addPacketHandler(this);
	}

	public void handlePacket(Packet p)
	{
		if (p instanceof PacketStartGame)
		{
			PacketStartGame psg = ((PacketStartGame)p);
			player = psg.getPlayerID();
		}
		if (p instanceof PacketAvailableBids)
		{
			PacketAvailableBids pav = ((PacketAvailableBids)p);
			List<Integer> bids = pav.getAvailableBids();
			Collections.shuffle(bids);
			System.out.println(p);
			System.out.println(bids.get(0));
			if (player == 0) conncection.closeRequest();
			conncection.sendPacket(new PacketBid(bids.get(0), player));
		}
		if (p instanceof PacketChange)
		{
			//System.out.println(p);
			conncection.sendPacket(p);
		}
		if (p instanceof PacketAvailableCalls)
		{
			System.out.println(p);
			PacketAvailableCalls pav = ((PacketAvailableCalls)p);
			List<Card> calls = pav.getAvailableCalls();
			Collections.shuffle(calls);
			System.out.println(calls.get(0));
			conncection.sendPacket(new PacketCall(calls.get(0), player));
		}
		if (p instanceof PacketTurn)
		{
			//System.out.println(((PacketTurn)p).getType());
		}
	}

	public void connectionClosed()
	{
	}
}
