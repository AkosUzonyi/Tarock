package com.tisza.tarock.message;

import com.tisza.tarock.proto.EventProto.*;

import java.util.*;

public class EventQueue
{
	private List<List<Event>> eventsForPlayers = new ArrayList<>();

	public List<Event> pollEventsForPlayer(int player)
	{
		List<Event> result = new ArrayList<>(eventsForPlayers.get(player));
		eventsForPlayers.get(player).clear();
		return result;
	}

	public abstract class Collector
	{
		protected abstract void collectEvent(Event event);

		public void startGame(int id, List<String> names)
		{
			Event.StartGame e = Event.StartGame.newBuilder()
					.setMyId(id)
					.addAllPlayerName(names)
					.build();
			collectEvent(Event.newBuilder().setStartGame(e).build());
		}

		public void startGame(int id, List<String> names)
		{
			Event.
			Event.StartGame e = Event.StartGame.newBuilder()
					.setMyId(id)
					.addAllPlayerName(names)
					.build();
			collectEvent(Event.newBuilder().setStartGame(e).build());
		}
	}
}
