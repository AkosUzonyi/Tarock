package com.tisza.tarock.player;

import com.tisza.tarock.message.*;

import java.util.concurrent.*;

public interface Player
{
	public String getName();
	public EventQueue getEventQueue();
	public void onJoinedToGame(BlockingQueue<Action> actionQueue, int playerID);
	public void onDisconnectedFromGame();
}
