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
					int userID = server.getFacebookUserManager().newAccessToken(fbAccessToken);
					newUser = server.getDatabase().getUser(userID);
				}

				if (newUser != null && server.isUserLoggedIn(newUser))
					newUser = null;

				MainProto.LoginResult.Builder loginMessageBuilder = MainProto.LoginResult.newBuilder();

				loggedInUser = newUser;
				if (loggedInUser != null)
				{
					server.loginUser(loggedInUser);
					loginMessageBuilder.setUserId(loggedInUser.getID());
				}

				sendMessage(MainProto.Message.newBuilder().setLoginResult(loginMessageBuilder.build()).build());

				server.broadcastStatus();

				break;
			}

			case ACTION:
				break;

			case CREATE_GAME:
			{
				MainProto.CreateGame createGame = message.getCreateGame();

				if (createGame.getUserIDCount() > 3)
					break;

				GameType gameType = GameType.fromID(createGame.getType());
				DoubleRoundType doubleRoundType = DoubleRoundType.fromID(createGame.getDoubleRoundType());

				List<User> users = new ArrayList<>();
				users.add(loggedInUser);
				for (int userID : createGame.getUserIDList())
				{
					users.add(server.getDatabase().getUser(userID));
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
								server.getDatabase().removeFCMToken(fcmToken);
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
					server.getGameSessionManager().removeKibic(currentGameID, currentPlayer);
				}

				if (message.getJoinGame().hasGameId())
				{
					currentGameID = message.getJoinGame().getGameId();
					currentPlayer = server.getGameSessionManager().getPlayer(currentGameID, loggedInUser);
					if (currentPlayer == null)
						currentPlayer = server.getGameSessionManager().addKibic(currentGameID, loggedInUser);
				}
				else
				{
					currentGameID = -1;
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
				if (active)
					server.getDatabase().addFCMToken(token, loggedInUser.getID());
				else
					server.getDatabase().removeFCMToken(token);
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
			server.logoutUser(loggedInUser);
			loggedInUser = null;
		}
	}
}
