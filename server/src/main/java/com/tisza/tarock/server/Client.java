package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;
import io.reactivex.Observable;
import io.reactivex.*;
import io.reactivex.schedulers.*;

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
				if (message.getLogin().hasFacebookToken())
				{
					String fbAccessToken = message.getLogin().getFacebookToken();
					server.getFacebookUserManager().newAccessToken(fbAccessToken)
							.subscribe(this::userLogin);
				}
				else
				{
					userLogin(null);
				}

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

				loggedInUser.getName().subscribe(loggedInUserName ->
				Observable.concat(Observable.just(loggedInUser.getID()), Observable.fromIterable(createGame.getUserIDList())).map(server.getDatabase()::getUser).toList().subscribe(users ->
				server.getGameSessionManager().createNewGame(gameType, users, doubleRoundType).subscribe(gameID ->
				{
					server.broadcastStatus();

					List<String> playerNames = server.getGameSessionManager().getPlayerNames(gameID);
					Flowable.fromIterable(users).flatMap(User::getFCMTokens).subscribe(fcmToken ->
					{
						Single.<Boolean>create(subscriber -> subscriber.onSuccess(server.getFirebaseNotificationSender().sendNewGameNotification(fcmToken, gameID, loggedInUserName, playerNames)))
								.subscribeOn(Schedulers.io())
								.subscribe(valid -> {if (!valid) server.getDatabase().removeFCMToken(fcmToken);});
					});
				})));

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
				if (message.getJoinGame().hasGameId())
				{
					int gameID = message.getJoinGame().getGameId();
					ProtoPlayer player = server.getGameSessionManager().getPlayer(gameID, loggedInUser);
					if (player != null)
						switchPlayer(gameID, player);
					else
						server.getGameSessionManager().addKibic(currentGameID, loggedInUser).subscribe(p -> switchPlayer(gameID, p));
				}
				else
				{
					switchPlayer(-1, null);
				}

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

	private void switchPlayer(int gameID, ProtoPlayer player)
	{
		if (currentPlayer != null)
		{
			currentPlayer.useConnection(null);
			server.getGameSessionManager().removeKibic(currentGameID, currentPlayer);
		}

		currentGameID = gameID;
		currentPlayer = player;

		if (currentPlayer != null)
			currentPlayer.useConnection(connection);
	}

	private void userLogin(User newUser)
	{
		if (connection == null)
			return;

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
