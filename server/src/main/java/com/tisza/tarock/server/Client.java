package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
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
	private int currentGameID = -1;

	public Client(Server server, ProtoConnection connection)
	{
		this.server = server;
		this.connection = connection;
		connection.addMessageHandler(this);
	}

	public User getLoggedInUser()
	{
		return loggedInUser;
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
				String fbAccessToken = null;
				User newUser = null;

				if (message.getLogin().hasFacebookToken())
				{
					fbAccessToken = message.getLogin().getFacebookToken();
					newUser = server.getFacebookUserManager().getUserByAccessToken(fbAccessToken);
				}

				if (newUser != null && newUser.isLoggedIn())
					newUser = null;

				MainProto.Login.Builder loginMessageBuilder = MainProto.Login.newBuilder();

				loggedInUser = newUser;
				if (loggedInUser != null)
				{
					loggedInUser.setLoggedIn(true);
					loginMessageBuilder.setFacebookToken(fbAccessToken);
				}

				sendMessage(MainProto.Message.newBuilder().setLogin(loginMessageBuilder.build()).build());

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

				if (createGame.getUserIDCount() > 3)
					break;

				GameType gameType = GameType.fromID(createGame.getType());
				DoubleRoundType doubleRoundType = DoubleRoundType.fromID(createGame.getDoubleRoundType());

				List<User> users = new ArrayList<>();
				users.add(loggedInUser);
				for (String userID : createGame.getUserIDList())
				{
					users.add(server.getFacebookUserManager().getUserByID(userID));
				}

				int gameID = server.getGameSessionManager().createNewGame(gameType, users, doubleRoundType);

				List<String> playerNames = server.getGameSessionManager().getPlayerNames(gameID);
				for (User user : users)
				{
					for (String fcmToken : new ArrayList<>(user.getFCMTokens()))
					{
						try
						{
							boolean valid = server.getFirebaseNotificationSender().sendNewGameNotification(fcmToken, gameID, loggedInUser.getName(), playerNames);
							if (!valid)
								server.getFacebookUserManager().registerFCMToken(fcmToken, null);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}

				server.broadcastStatus();

				break;
			}

			case DELETE_GAME:
			{
				int gameID = message.getDeleteGame().getGameId();

				if (server.getGameSessionManager().isGameOwnedBy(gameID, loggedInUser))
				{
					server.getGameSessionManager().deleteGame(gameID);
				}

				server.broadcastStatus();
			}

			case JOIN_GAME:
			{
				if (currentPlayer != null)
				{
					currentPlayer.useConnection(null);
					server.getGameSessionManager().removeKibic(loggedInUser, currentGameID);
				}

				if (message.getJoinGame().hasGameId())
				{
					currentGameID = message.getJoinGame().getGameId();
					currentPlayer = loggedInUser.getPlayerForGame(currentGameID);
					if (currentPlayer == null)
						currentPlayer = server.getGameSessionManager().addKibic(loggedInUser, currentGameID);
				}
				else
				{
					currentPlayer = null;
				}

				if (currentPlayer != null)
					currentPlayer.useConnection(connection);

				break;
			}

			case FCM_TOKEN:
			{
				String token = message.getFcmToken().getFcmToken();
				boolean active = message.getFcmToken().getActive();
				server.getFacebookUserManager().registerFCMToken(token, active ? loggedInUser : null);
				break;
			}

			default:
				System.err.println("unhandled message type: " + message.getMessageTypeCase());
				break;
		}
	}

	public void sendMessage(MainProto.Message message)
	{
		connection.sendMessage(message);
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
