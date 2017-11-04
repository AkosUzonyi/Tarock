package com.tisza.tarock.net;

import com.tisza.tarock.net.packet.Packet;


public interface PacketHandler
{
	public void handlePacket(Packet p);
	public void connectionClosed();
}
