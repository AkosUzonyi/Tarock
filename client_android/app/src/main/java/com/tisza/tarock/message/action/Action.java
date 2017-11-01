package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.ActionHandler;

public abstract class Action
{
	protected Action(){}
	
	public abstract void writeData(DataOutputStream dos) throws IOException;
	public abstract void readData(DataInputStream dis) throws IOException;
	public abstract void handle(int player, ActionHandler handler);
}
