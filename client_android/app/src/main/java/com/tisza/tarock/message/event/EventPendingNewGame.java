package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventPendingNewGame extends Event
{
	public EventPendingNewGame() {}

	public void readData(DataInputStream dis) throws IOException
	{
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
	}

	public void handle(EventHandler handler)
	{
		handler.pendingNewGame();
	}
}
