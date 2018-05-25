package com.tisza.tarock.player.proto;

import com.tisza.tarock.message.*;
import com.tisza.tarock.player.*;
import com.tisza.tarock.proto.*;

import java.util.concurrent.*;

public class ProtoPlayer implements Player
{
	private ProtoConnection connection;
	private EventSender eventSender;

	private JoinRequestHandler joinRequestHandler = null;

	private String name = null;

	private int playerID;
	private BlockingQueue<Action> actionQueue = null;

	public ProtoPlayer(ProtoConnection connection)
	{
		this.connection = connection;
		eventSender = new ProtoEventSender(connection);
		connection.addMessageHandler(new MyMessageHandler());
	}

	public String getName()
	{
		return name;
	}

	public EventSender getEventSender()
	{
		return eventSender;
	}

	public void setJoinRequestHandler(JoinRequestHandler joinRequestHandler)
	{
		this.joinRequestHandler = joinRequestHandler;
	}

	public void onJoinedToGame(BlockingQueue<Action> actionQueue, int playerID)
	{
		this.actionQueue = actionQueue;
		this.playerID = playerID;
	}

	public void onDisconnectedFromGame()
	{
		actionQueue = null;
		//TODO: remove
		connection.closeRequest();
	}

	private class MyMessageHandler implements MessageHandler
	{
		public void handleMessage(MainProto.Message message)
		{
			switch (message.getMessageTypeCase())
			{
				case ACTION:
					if (actionQueue == null)
						throw new IllegalStateException("no action queue");

					actionQueue.add(new ProtoAction(playerID, message.getAction()));
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

		public void connectionClosed()
		{
			System.out.println(name + " disconnected.");
		}
	}
}
