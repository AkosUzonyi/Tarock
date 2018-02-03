package com.tisza.tarock.message;

import java.util.concurrent.*;

public interface Player
{
	public String getName();
	public EventQueue getEventQueue();
	public void setActionQueue(BlockingQueue<Action> actionQueue);
}
