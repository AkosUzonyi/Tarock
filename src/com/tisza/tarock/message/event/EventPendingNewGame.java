package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.EventHandler;

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
