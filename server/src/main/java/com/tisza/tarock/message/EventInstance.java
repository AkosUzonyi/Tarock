package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

public class EventInstance
{
	private PlayerSeat playerSeat;
	private Event event;

	public EventInstance(PlayerSeat playerSeat, Event event)
	{
		this.playerSeat = playerSeat;
		this.event = event;
	}

	public static EventInstance broadcast(Event event)
	{
		return new EventInstance(null, event);
	}

	public PlayerSeat getPlayerSeat()
	{
		return playerSeat;
	}

	public Event getEvent()
	{
		return event;
	}
}
