package com.tisza.tarock.message.action;

import com.tisza.tarock.message.*;

import java.io.*;

public class ActionThrowCards extends Action
{
	public ActionThrowCards() {}

	public void readData(DataInputStream dis) {}
	public void writeData(DataOutputStream dos) {}

	public void handle(int player, ActionHandler handler)
	{
		handler.throwCards(player);
	}
}
