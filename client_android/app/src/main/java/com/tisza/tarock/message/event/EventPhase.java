package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.game.PhaseEnum;
import com.tisza.tarock.message.EventHandler;

public class EventPhase extends Event
{
	private PhaseEnum phase;
	
	public EventPhase() {}
	
	public EventPhase(PhaseEnum phase)
	{
		this.phase = phase;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		phase = PhaseEnum.values()[dis.readByte()];
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(phase.ordinal());
	}

	public void handle(EventHandler handler)
	{
		handler.phaseChanged(phase);
	}
}
