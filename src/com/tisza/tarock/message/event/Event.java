package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.EventHandler;

public abstract class Event
{
	private int target;
	
	public Event(){}
	
	public int getTarget()
	{
		return target;
	}
	
	public Event setTarget(int target)
	{
		this.target = target;
		return this;
	}
	
	public abstract void writeData(DataOutputStream dos) throws IOException;
	public abstract void readData(DataInputStream dis) throws IOException;
	public abstract void handle(EventHandler handler);
}
