package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class EventStartGame extends Event
{
	private int myID;
	private List<String> playerNames;
	
	public EventStartGame() {}
	
	public EventStartGame(int myID, List<String> playerNames) 
	{
		this.myID = myID;
		this.playerNames = playerNames;
	}
	
	public void readData(DataInputStream dis) throws IOException
	{
		myID = dis.readByte();
		playerNames = new ArrayList<String>(4);
		for (int i = 0; i < 4; i++)
		{
			playerNames.add(dis.readUTF());
		}
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(myID);
		for (int i = 0; i < 4; i++)
		{
			dos.writeUTF(playerNames.get(i));
		}
	}

	public void handle(EventHandler handler)
	{
		handler.startGame(myID, playerNames);
	}
}
