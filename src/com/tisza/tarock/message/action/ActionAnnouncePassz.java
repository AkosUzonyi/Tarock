package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.tisza.tarock.message.ActionHandler;

public class ActionAnnouncePassz extends Action
{
	public ActionAnnouncePassz() {}

	public void writeData(DataOutputStream dos) {}
	public void readData(DataInputStream dis) {}

	public void handle(int player, ActionHandler handler)
	{
		handler.announcePassz(player);
	}
}
