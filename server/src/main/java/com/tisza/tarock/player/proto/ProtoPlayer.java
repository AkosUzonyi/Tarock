package com.tisza.tarock.player.proto;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.player.*;
import com.tisza.tarock.proto.*;

import java.io.*;
import java.util.concurrent.*;

public class ProtoPlayer implements Player
{
	private ProtoConnection connection;
	private EventSender eventSender;

	private JoinRequestHandler joinRequestHandler = null;

	private String name = null;

	private PlayerSeat seat;
	private BlockingQueue<Action> actionQueue = null;

	public ProtoPlayer(ProtoConnection connection)
	{
		this.connection = connection;
		eventSender = new ProtoEventSender(connection);
		connection.addMessageHandler(new MyMessageHandler());
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public EventSender getEventSender()
	{
		return eventSender;
	}

	public void setJoinRequestHandler(JoinRequestHandler joinRequestHandler)
	{
		this.joinRequestHandler = joinRequestHandler;
	}

	@Override
	public void onJoinedToGame(BlockingQueue<Action> actionQueue, PlayerSeat seat)
	{
		this.actionQueue = actionQueue;
		this.seat = seat;
	}

	@Override
	public void onDisconnectedFromGame()
	{
		actionQueue = null;
		//TODO: remove
		try
		{
			connection.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private class MyMessageHandler implements MessageHandler
	{
		@Override
		public void handleMessage(MainProto.Message message)
		{
			switch (message.getMessageTypeCase())
			{
				case ACTION:
					if (actionQueue == null)
						throw new IllegalStateException("no action queue");

					actionQueue.add(new ProtoAction(seat, message.getAction()));
					break;
				case LOGIN:
					name = message.getLogin().getName();
					System.out.println("Player logged in: " + name);
					joinRequestHandler.requestJoin(0);
					break;
				default:
					System.err.println("unhandled message type: " + message.getMessageTypeCase());
			}
		}

		@Override
		public void connectionClosed()
		{
			System.out.println(name + " disconnected.");
		}
	}
}
