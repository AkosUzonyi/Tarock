package com.tisza.tarock.message.action;

import com.tisza.tarock.message.*;

import java.io.*;

public abstract class Action
{
	protected Action(){}
	
	public abstract void writeData(DataOutputStream dos) throws IOException;
	public abstract void readData(DataInputStream dis) throws IOException;
	public abstract void handle(int player, ActionHandler handler);
}
