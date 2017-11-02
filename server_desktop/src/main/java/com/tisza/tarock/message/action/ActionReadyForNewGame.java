package com.tisza.tarock.message.action;

import com.tisza.tarock.message.*;

import java.io.*;

public class ActionReadyForNewGame extends Action
{
	public ActionReadyForNewGame() {}

	public void readData(DataInputStream dis) {}
	public void writeData(DataOutputStream dos) {}

	public void handle(int player, ActionHandler handler)
	{
		handler.readyForNewGame(player);
	}
}
