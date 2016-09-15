package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.net.packet.*;

public interface GamePhase
{
	public void start();
	public void playerLoggedIn(int player);
	public void packetFromPlayer(int player, Packet packet);
}
