package com.tisza.tarock;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.net.*;
import com.tisza.tarock.server.player.*;
import org.apache.log4j.*;

import java.util.*;

public class StressConnection implements MessageHandler
{
	private static final Logger log = Logger.getLogger(StressConnection.class);

	private int user;
	private boolean createGame;
	private ProtoConnection connection;

	private Player player;

	public StressConnection(ProtoConnection connection, int user, boolean createGame)
	{
		this.user = user;
		this.createGame = createGame;
		this.connection = connection;

		double actionPerSecond = 5;
		int delay = (int)(1000 / (new Random().nextGaussian() / 100 + actionPerSecond));

		player = new RandomPlayer(null, null, delay, delay)
		{
			@Override
			protected void doAction(Action action)
			{
				connection.sendMessage(MainProto.Message.newBuilder().setAction(action.getId()).build());
			}
		};
	}

	public void start()
	{
		connection.addMessageHandler(this);
		connection.sendMessage(MainProto.Message.newBuilder().setLogin(MainProto.Login.newBuilder().setTestId(user).build()).build());
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		switch (message.getMessageTypeCase())
		{
			case LOGIN_RESULT:
				log.info(createGame);
				if (createGame)
					connection.sendMessage(MainProto.Message.newBuilder().setCreateGameSession(MainProto.CreateGameSession.newBuilder().setType("magas").setDoubleRoundType("none").addAllUserID(Arrays.asList(user, user + 1, user + 2, user + 3)).build()).build());

				break;

			case EVENT:
				player.handleEvent(new ProtoEvent(message.getEvent()));
				break;

			case SERVER_STATUS:
				if (player.getSeat() != null)
					break;

				for (MainProto.GameSession gameSession : message.getServerStatus().getAvailableGameSessionList())
				{
					int seat = gameSession.getUserIdList().indexOf(user);
					if (seat >= 0)
					{
						connection.sendMessage(MainProto.Message.newBuilder().setJoinGameSession(MainProto.JoinGameSession.newBuilder().setGameSessionId(gameSession.getId()).build()).build());
						player.setGame(null, PlayerSeat.fromInt(seat));
					}
				}
				break;
		}
	}

	@Override
	public void connectionClosed()
	{

	}
}
