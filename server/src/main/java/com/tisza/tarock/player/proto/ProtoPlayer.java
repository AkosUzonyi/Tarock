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
	private ProtoEventSender eventSender = new ProtoEventSender();
	private MyMessageHandler messageHandler = new MyMessageHandler();

	private String name;

	private PlayerSeat seat;
	private BlockingQueue<Action> actionQueue = null;

	public ProtoPlayer(String name)
	{
		this.name = name;
		useConnection(null);
	}

	public void useConnection(ProtoConnection connection)
	{
		this.connection = connection;
		eventSender.useConnection(connection);

		if (connection != null)
		{
			connection.addMessageHandler(messageHandler);

			if (actionQueue != null)
				actionQueue.add(handler -> handler.requestHistory(seat));
		}
	}

	public boolean isConnected()
	{
		return connection != null;
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
			if (connection != null)
			{
				connection.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			connection = null;
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
					if (actionQueue != null)
						actionQueue.add(new ProtoAction(seat, message.getAction()));
					break;
				case LOGIN:
					//name = message.getLogin().getName();
					System.out.println("Player logged in: " + name);
					break;
				default:
					System.err.println("unhandled message type: " + message.getMessageTypeCase());
			}
		}

		@Override
		public void connectionClosed()
		{
			useConnection(null);
			System.out.println(name + " disconnected.");
		}
	}
}
