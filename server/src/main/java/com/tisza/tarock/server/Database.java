package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.message.*;
import io.reactivex.*;
import io.reactivex.schedulers.*;
import org.flywaydb.core.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Database
{
	private static final String DATABASE_FILENAME = "tarock.db";

	private final String dbURL;
	private final Scheduler observerScheduler;
	private org.davidmoten.rx.jdbc.Database rxdatabase;

	public Database(File dbDir, Executor observerExecutor)
	{
		dbURL = "jdbc:sqlite:" + new File(dbDir, DATABASE_FILENAME).getAbsolutePath();
		observerScheduler = Schedulers.from(observerExecutor);
	}

	public void initialize()
	{
		if (rxdatabase != null)
			throw new IllegalStateException();

		Flyway flyway = Flyway.configure().dataSource(dbURL, null, null).load();
		flyway.migrate();

		rxdatabase = org.davidmoten.rx.jdbc.Database.from(dbURL, 1);
		rxdatabase.update("PRAGMA foreign_keys = ON").complete().subscribe();
	}

	private <T> SingleTransformer<T, T> resultTransformerUpdateSingle()
	{
		return upstream ->
		{
			upstream = upstream.cache();
			upstream.subscribe();
			return upstream.observeOn(observerScheduler);
		};
	}

	private <T> SingleTransformer<T, T> resultTransformerQuerySingle()
	{
		return upstream -> upstream.observeOn(observerScheduler);
	}

	private <T> FlowableTransformer<T, T> resultTransformerQueryFlowable()
	{
		return upstream -> upstream.observeOn(observerScheduler);
	}

	public Single<Integer> setFacebookUserData(String facebookId, String name, String imgURL, List<String> friendFacebookIDs)
	{
		Single<Integer> selectID = rxdatabase.select("SELECT id FROM user WHERE facebook_id = ?")
				.parameter(facebookId).getAs(Integer.class).singleOrError();

		Single<Integer> insert = rxdatabase.update("INSERT INTO user(facebook_id, name, img_url, registration_time) VALUES (?, ?, ?, ?);")
				.parameters(facebookId, name, imgURL, System.currentTimeMillis())
				.returnGeneratedKeys().getAs(Integer.class).singleOrError();

		Single<Integer> userID = rxdatabase.update("UPDATE user SET name = ?, img_url = ? where facebook_id = ?")
				.parameters(name, imgURL, facebookId)
				.counts().flatMapSingle(count -> count == 0 ? insert : selectID)
				.singleOrError().cache();

		if (friendFacebookIDs != null)
		{
			userID.subscribe(uid ->
			{
				Flowable<Object> parameters = Flowable.fromIterable(friendFacebookIDs)
						.flatMap(friendFacebookID -> Flowable.just(friendFacebookID, uid, uid));

				rxdatabase.update("DELETE FROM friendship WHERE id0 = ? OR id1 = ?;")
						.parameters(uid, uid)
						.complete()
						.andThen(
							rxdatabase.update("WITH friend AS (SELECT id FROM user WHERE facebook_id = ?) " +
									"INSERT INTO friendship(id0, id1) SELECT ?, id FROM friend UNION SELECT id, ? FROM friend;")
									.batchSize(friendFacebookIDs.size())
									.parameterStream(parameters).complete()
						).subscribe();
			});
		}

		return userID.compose(resultTransformerUpdateSingle());
	}

	public User getUser(int userID)
	{
		return new User(userID, this);
	}

	public Single<String> getUserName(int userID)
	{
		return rxdatabase.select("SELECT name FROM user WHERE id = ?;")
				.parameter(userID).getAs(String.class).singleOrError()
				.compose(resultTransformerQuerySingle());
	}

	public Single<String> getUserImgURL(int userID)
	{
		return rxdatabase.select("SELECT img_url FROM user WHERE id = ?;")
				.parameter(userID).getAs(String.class).singleOrError()
				.compose(resultTransformerQuerySingle());
	}

	public Single<Boolean> areUserFriends(int userID0, int userID1)
	{
		return rxdatabase.select("SELECT COUNT(id0) FROM friendship WHERE id0 = ? AND id1 = ?;")
				.parameters(userID0, userID1).getAs(Integer.class).singleOrError().map(count -> count > 0)
				.compose(resultTransformerQuerySingle());
	}

	public Flowable<User> getUsers()
	{
		return rxdatabase.select("SELECT id FROM user ORDER BY id;")
				.getAs(Integer.class).map(id -> new User(id, this))
				.compose(resultTransformerQueryFlowable());
	}

	public Single<Integer> createGameSession(GameType gameType, DoubleRoundType doubleRoundType)
	{
		return rxdatabase.update("INSERT INTO game_session(type, double_round_type, create_time) VALUES(?, ?, ?);")
				.parameters(gameType.getID(), doubleRoundType.getID(), System.currentTimeMillis())
				.returnGeneratedKeys().getAs(Integer.class).singleOrError()
				.compose(resultTransformerUpdateSingle());
	}

	public Flowable<Integer> getActiveGameSessionIDs()
	{
		return rxdatabase.select("SELECT id FROM game_session WHERE current_game_id IS NOT NULL;")
				.getAs(Integer.class)
				.compose(resultTransformerQueryFlowable());
	}

	public void stopGameSession(int gameSessionID)
	{
		rxdatabase.update("UPDATE game_session SET current_game_id = NULL where id = ?;")
				.parameter(gameSessionID).complete().subscribe();
	}

	public void addBotPlayer(int gameSessionID, int seat)
	{
		addUserPlayer(gameSessionID, seat, null);
	}

	public void addUserPlayer(int gameSessionID, int seat, Integer userID)
	{
		rxdatabase.update("INSERT INTO player(game_session_id, seat, user_id) VALUES(?, ?, ?);")
				.parameters(gameSessionID, seat, userID).complete().subscribe();
	}

	public void setPlayerPoints(int gameSessionID, int seat, int value)
	{
		rxdatabase.update("UPDATE player SET points = ? WHERE game_session_id = ? AND seat = ?;")
				.parameters(value, gameSessionID, seat).complete().subscribe();
	}

	public Single<Integer> createGame(int gameSessionID, PlayerSeat beginnerPlayer)
	{
		return rxdatabase.update("INSERT INTO game(game_session_id, beginner_player, create_time) VALUES(?, ?, ?);")
				.parameters(gameSessionID, beginnerPlayer.asInt(), System.currentTimeMillis())
				.returnGeneratedKeys().getAs(Integer.class).singleOrError()
				.doOnSuccess(gameID ->
				{
					rxdatabase.update("UPDATE game_session SET current_game_id = ? where id = ?;")
							.parameters(gameID, gameSessionID).complete().subscribe();
				})
				.compose(resultTransformerUpdateSingle());
	}

	public void setDeck(int gameID, List<Card> deck)
	{
		Flowable<Object> parameters = Flowable.range(0, deck.size())
				.flatMap(i -> Flowable.just(gameID, i, deck.get(i).getID()));

		rxdatabase.update("INSERT INTO deck_card(game_id, ordinal, card) VALUES(?, ?, ?);")
				.batchSize(deck.size()).parameterStream(parameters)
				.complete().subscribe();
	}

	public Flowable<Card> getDeck(int gameID)
	{
		return rxdatabase.select("SELECT card FROM deck_card WHERE game_id = ? ORDER BY ordinal;")
				.parameter(gameID).getAs(String.class).map(Card::fromId)
				.compose(resultTransformerQueryFlowable());
	}

	public void addAction(int gameID, Action action, int ordinal)
	{
		rxdatabase.update("INSERT INTO action(game_id, ordinal, seat, action, time) VALUES(?, ?, ?, ?, ?);")
				.parameters(gameID, ordinal, action.getPlayer().asInt(), action.getId(), System.currentTimeMillis()).complete().subscribe();
	}

	public Flowable<Action> getActions(int gameID)
	{
		return rxdatabase.select("SELECT seat, action FROM action WHERE game_id = ? ORDER BY ordinal;")
				.parameter(gameID).getAs(Integer.class, String.class)
				.map(tuple -> new Action(PlayerSeat.fromInt(tuple._1()), tuple._2()))
				.compose(resultTransformerQueryFlowable());
	}

	public void addFCMToken(String token, int userID)
	{
		Flowable<Integer> insert = rxdatabase.update("INSERT INTO fcm_token(token, user_id) VALUES (?, ?);")
				.parameters(token, userID).counts();

		rxdatabase.update("UPDATE fcm_token SET user_id = ? WHERE token = ?;")
				.parameters(userID, token).counts()
				.flatMap(count -> count == 0 ? insert : Flowable.empty())
				.subscribe();
	}

	public void removeFCMToken(String token)
	{
		rxdatabase.update("DELETE FROM fcm_token WHERE token = ?;")
				.parameter(token).complete().subscribe();
	}

	public Flowable<String> getFCMTokensForUser(int userID)
	{
		return rxdatabase.select("SELECT token FROM fcm_token WHERE user_id = ?;")
				.parameter(userID).getAs(String.class)
				.compose(resultTransformerQueryFlowable());
	}

	public void shutdown()
	{
		if (rxdatabase != null)
			rxdatabase.close();
		rxdatabase = null;
	}
}
