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
	private GameSession currentGameSession;

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

				if (createGame.getUserIDCount() != 3)
					break;

				GameType gameType = GameType.fromID(createGame.getType());
				DoubleRoundType doubleRoundType = DoubleRoundType.fromID(createGame.getDoubleRoundType());

				loggedInUser.getName().subscribe(loggedInUserName ->
				Observable.concat(Observable.just(loggedInUser.getID()), Observable.fromIterable(createGame.getUserIDList())).map(server.getDatabase()::getUser).toList().subscribe(users ->
				server.getGameSessionManager().createGameSession(gameType, users, doubleRoundType).subscribe(gameSession ->
				{
					server.broadcastStatus();

					List<String> playerNames = gameSession.getPlayerNames();
					Flowable.fromIterable(users).flatMap(User::getFCMTokens).subscribe(fcmToken ->
					{
						Single.<Boolean>create(subscriber -> subscriber.onSuccess(server.getFirebaseNotificationSender().sendNewGameNotification(fcmToken, gameSession.getID(), loggedInUserName, playerNames)))
								.subscribeOn(Schedulers.io())
								.subscribe(valid -> {if (!valid) server.getDatabase().removeFCMToken(fcmToken);});
					});
				})));

				break;
			}

			case DELETE_GAME:
			{
				int gameSessionID = message.getDeleteGame().getGameId();

				if (server.getGameSessionManager().getGameSession(gameSessionID).isUserPlaying(loggedInUser))
					server.getGameSessionManager().stopGameSession(gameSessionID);

				server.broadcastStatus();
			}

			case JOIN_GAME:
			{
				if (!message.getJoinGame().hasGameId())
				{
					switchGameSession(null, null);
					break;
				}

				GameSession gameSession = server.getGameSessionManager().getGameSession(message.getJoinGame().getGameId());
				if (gameSession.isUserPlaying(loggedInUser))
				{
					ProtoPlayer player = (ProtoPlayer)gameSession.getPlayerByUser(loggedInUser);
					switchGameSession(gameSession, player);
				}
				else
				{
					loggedInUser.createPlayer().subscribe(player ->
					{
						gameSession.addKibic(player);
						switchGameSession(gameSession, (ProtoPlayer)player);
					});
				}

				break;
			}

			case FCM_TOKEN:
			{
				String token = message.getFcmToken().getFcmToken();
				boolean active = message.getFcmToken().getActive();
				if (active)
					server.getDatabase().addFCMToken(token, loggedInUser);
				else
					server.getDatabase().removeFCMToken(token);
				break;
			}

			default:
				System.err.println("unhandled message type: " + message.getMessageTypeCase());
				break;
		}
	}

	private void switchGameSession(GameSession gameSession, ProtoPlayer player)
	{
		if (currentPlayer != null)
		{
			currentPlayer.useConnection(null);
			currentGameSession.removeKibic(currentPlayer);
		}

		currentGameSession = gameSession;
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
