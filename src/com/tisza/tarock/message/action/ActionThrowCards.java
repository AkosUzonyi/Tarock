package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.tisza.tarock.message.ActionHandler;

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
