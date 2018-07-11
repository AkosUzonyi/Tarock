package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import java.io.*;
import java.util.*;

public class Client implements MessageHandler
{
	private final Server server;
	private ProtoConnection connection;
	private User loggedInUser = null;
	private ProtoPlayer currentPlayer = null;

	public Client(Server server, ProtoConnection connection)
	{
		this.server = server;
		this.connection = connection;
		connection.addMessageHandler(this);
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		if (loggedInUser == null && message.getMessageTypeCase() != MainProto.Message.MessageTypeCase.LOGIN)
			return;

		switch (message.getMessageTypeCase())
		{
			case LOGIN:
			{
				String fbAccessToken = message.getLogin().getFacebookToken();
				User newUser = server.getFacebookUserManager().getUserByAccessToken(fbAccessToken);

				if (newUser == null || newUser.isLoggedIn())
				{
					server.removeClient(this);
					break;
				}

				loggedInUser = newUser;
				newUser.setLoggedIn(true);

				server.broadcastStatus();

				break;
			}

			case ACTION:
			{
				if (currentPlayer == null)
					break;

				currentPlayer.queueAction(message.getAction());
				break;
			}

			case CREATE_GAME:
			{
				MainProto.CreateGame createGame = message.getCreateGame();

				if (createGame.getUserIDCount() > 4)
					break;

				GameType gameType = Utils.gameTypeFromProto(createGame.getType());

				List<User> users = new ArrayList<>();
				for (String userID : createGame.getUserIDList())
				{
					users.add(server.getFacebookUserManager().getUserByID(userID));
				}

				int gameID = server.getGameSessionManager().createNewGame(gameType, users);

				server.broadcastStatus();

				break;
			}

			case DELETE_GAME:
			{
				int gameID = message.getDeleteGame().getGameId();

				if (server.getGameSessionManager().hasUserPermissionToDelete(gameID, loggedInUser))
				{
					server.getGameSessionManager().deleteGame(gameID);
				}

				server.broadcastStatus();
			}

			case JOIN_GAME:
			{
				if (currentPlayer != null)
					currentPlayer.useConnection(null);

				if (message.getJoinGame().hasGameId())
				{
					int gameID = message.getJoinGame().getGameId();
					currentPlayer = loggedInUser.getPlayerForGame(gameID);
				}
				else
				{
					currentPlayer = null;
				}

				if (currentPlayer != null)
					currentPlayer.useConnection(connection);

				break;
			}

			default:
				System.err.println("unhandled message type: " + message.getMessageTypeCase());
				break;
		}
	}

	public void sendStatus()
	{
		MainProto.ServerStatus.Builder builder = MainProto.ServerStatus.newBuilder();

		for (GameInfo gameInfo : server.getGameSessionManager().listGames())
		{
			builder.addAvailableGame(Utils.gameInfoToProto(gameInfo));
		}

		for (User user : server.getFacebookUserManager().listUsers())
		{
			builder.addAvailableUser(Utils.userToProto(user, loggedInUser.isFriendWith(user)));
		}

		connection.sendMessage(MainProto.Message.newBuilder().setServerStatus(builder.build()).build());
	}

	@Override
	public void connectionClosed()
	{
		server.removeClient(this);
	}

	public void disonnect()
	{
		try
		{
			connection.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		connection = null;
		currentPlayer = null;
		if (loggedInUser != null)
		{
			loggedInUser.setLoggedIn(false);
			loggedInUser = null;
		}
	}
}
