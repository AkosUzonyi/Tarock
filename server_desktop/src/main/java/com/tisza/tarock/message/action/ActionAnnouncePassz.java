package com.tisza.tarock.message.action;

import com.tisza.tarock.message.*;

import java.io.*;

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
