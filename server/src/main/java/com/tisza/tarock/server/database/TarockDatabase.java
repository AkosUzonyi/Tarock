package com.tisza.tarock.server.database;

import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.message.*;
import io.reactivex.*;
import io.reactivex.schedulers.*;
import org.apache.log4j.*;
import org.davidmoten.rx.jdbc.*;
import org.davidmoten.rx.jdbc.tuple.*;
import org.flywaydb.core.*;

import java.io.*;
import java.util.*;

public class TarockDatabase
{
	private static final Logger log = Logger.getLogger(TarockDatabase.class);

	private final String dbURL;
	private final Scheduler observerScheduler;
	private Database rxdatabase;

	public TarockDatabase()
	{
		dbURL = "jdbc:mysql://localhost/tarock?user=tarock&password=skiz&serverTimezone=UTC";
		observerScheduler = Schedulers.from(Main.GAME_EXECUTOR_SERVICE);
	}

	public void initialize()
	{
		if (rxdatabase != null)
			throw new IllegalStateException();

		Flyway flyway = Flyway.configure().dataSource(dbURL, null, null).load();
		flyway.migrate();

		rxdatabase = Database.from(dbURL, 50);
	}

	private void logException(Throwable e, StackTraceElement[] stackTrace)
	{
		RuntimeException runtimeException = new RuntimeException(e);
		runtimeException.setStackTrace(stackTrace);
		log.error(null, runtimeException);
	}

	private CompletableTransformer resultTransformerUpdateCompletable()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		return upstream ->
		{
			upstream = upstream.cache().observeOn(observerScheduler);
			upstream.subscribe(() -> {}, e -> logException(e, stackTrace));
			return upstream;
		};
	}

	private <T> SingleTransformer<T, T> resultTransformerUpdateSingle()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		return upstream ->
		{
			upstream = upstream.cache().observeOn(observerScheduler);
			upstream.subscribe(result -> {}, e -> logException(e, stackTrace));
			return upstream;
		};
	}

	private <T> SingleTransformer<T, T> resultTransformerQuerySingle()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		return upstream -> upstream
				.doOnError(e -> logException(e, stackTrace))
				.observeOn(observerScheduler);
	}

	private <T> FlowableTransformer<T, T> resultTransformerQueryFlowable()
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		return upstream -> upstream
				.doOnError(e -> logException(e, stackTrace))
				.observeOn(observerScheduler);
	}

	public Single<User> setFacebookUserData(String facebookId, String name, String imgURL, List<String> friendFacebookIDs)
	{
		Single<Integer> userID = setIdentityProviderData("facebook", facebookId, name, imgURL);

		if (friendFacebookIDs != null)
		{
			userID = userID.doOnSuccess(uid ->
			{
				Flowable<Object> parameters = Flowable.fromIterable(friendFacebookIDs)
						.flatMap(friendFacebookID -> Flowable.just(friendFacebookID, uid, uid));

				rxdatabase.update("DELETE FROM friendship WHERE id0 = ? OR id1 = ?;")
						.parameters(uid, uid)
						.complete()
						.andThen(
							rxdatabase.update("WITH friend AS (SELECT user_id FROM idp_user WHERE idp_service_id = \"facebook\" AND idp_user_id = ?) " +
									"INSERT INTO friendship(id0, id1) SELECT ?, user_id FROM friend UNION SELECT user_id, ? FROM friend;")
									.batchSize(friendFacebookIDs.size())
									.parameterStream(parameters).complete()
						).compose(resultTransformerUpdateCompletable());
			});
		}

		return userID.map(this::getUser).compose(resultTransformerUpdateSingle());
	}

	public Flowable<Tuple2<String, User>> getFacebookUsers()
	{
		return rxdatabase.select("SELECT idp_user_id, user_id FROM idp_user WHERE idp_service_id = \"facebook\"")
				.getAs(String.class, Integer.class).map(tuple -> Tuple2.create(tuple._1(), getUser(tuple._2()))).compose(resultTransformerQueryFlowable());
	}

	public Single<User> setGoogleUserData(String id, String name, String imgURL)
	{
		Single<Integer> userID = setIdentityProviderData("google", id, name, imgURL);

		return userID.map(this::getUser).compose(resultTransformerUpdateSingle());
	}

	private Single<Integer> setIdentityProviderData(String idpServiceID, String idpUserID, String name, String imgURL)
	{
		Single<Integer> selectID = rxdatabase.select("SELECT user_id FROM idp_user WHERE idp_service_id = ? AND idp_user_id = ?")
				.parameters(idpServiceID, idpUserID).getAs(Integer.class).singleOrError();

		Single<Integer> insert = rxdatabase.update("INSERT INTO user(name, img_url, registration_time) VALUES (?, ?, ?);")
				.parameters(name, imgURL, System.currentTimeMillis())
				.returnGeneratedKeys().getAs(Integer.class).singleOrError()
				.doOnSuccess(userID -> rxdatabase.update("INSERT INTO idp_user(idp_service_id, idp_user_id, user_id) VALUES(?, ?, ?)")
						.parameters(idpServiceID, idpUserID, userID).complete()
						.compose(resultTransformerUpdateCompletable()));

		Single<Integer> userID = rxdatabase.update("UPDATE user SET name = ?, img_url = ? WHERE id = (SELECT user_id FROM idp_user WHERE idp_service_id = ? AND idp_user_id = ?)")
				.parameters(name, imgURL, idpServiceID, idpUserID).counts().singleOrError()
				.flatMap(count -> count == 0 ? insert : selectID);

		return userID;
	}

	public User getUser(int userID)
	{
		return new User(userID, this);
	}

	Single<String> getUserName(int userID)
	{
		return rxdatabase.select("SELECT name FROM user WHERE id = ?;")
				.parameter(userID).getAs(String.class).singleOrError()
				.compose(resultTransformerQuerySingle());
	}

	Single<Optional<String>> getUserImgURL(int userID)
	{
		return rxdatabase.select("SELECT img_url FROM user WHERE id = ?;")
				.parameter(userID).getAsOptional(String.class).singleOrError()
				.compose(resultTransformerQuerySingle());
	}

	Completable setUserImgURL(int userID, String imgURL)
	{
		return rxdatabase.update("UPDATE user SET img_url = ? WHERE id = ?;")
				.parameters(imgURL, userID).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	Single<Boolean> areUserFriends(int userID0, int userID1)
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

	public Flowable<User> getBotUsers()
	{
		return rxdatabase.select("SELECT id FROM user WHERE id < 4 ORDER BY id;")
				.getAs(Integer.class).map(id -> new User(id, this))
				.compose(resultTransformerQueryFlowable());
	}

	public Single<Integer> createGameSession(GameType gameType, DoubleRoundTracker doubleRoundTracker)
	{
		return rxdatabase.update("INSERT INTO game_session(type, double_round_type, double_round_data, create_time) VALUES(?, ?, ?, ?);")
				.parameters(gameType.getID(), doubleRoundTracker.getType().getID(), doubleRoundTracker.getData(), System.currentTimeMillis())
				.returnGeneratedKeys().getAs(Integer.class).singleOrError()
				.compose(resultTransformerUpdateSingle());
	}

	public Completable setDoubleRoundData(int gameSessionID, int data)
	{
		return rxdatabase.update("UPDATE game_session SET double_round_data = ? WHERE id = ?;")
				.parameters(data, gameSessionID).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Single<Tuple5<GameType, DoubleRoundType, Integer, Integer, Long>> getGameSession(int gameSessionID)
	{
		return rxdatabase.select("SELECT type, double_round_type, double_round_data, current_game_id, create_time FROM game_session WHERE id = ?;")
				.parameter(gameSessionID).getAs(String.class, String.class, Integer.class, Integer.class, Long.class).singleOrError()
				.map(tuple -> Tuple5.create(GameType.fromID(tuple._1()), DoubleRoundType.fromID(tuple._2()), tuple._3(), tuple._4(), tuple._5()))
				.compose(resultTransformerQuerySingle());
	}

	public Flowable<Integer> getActiveGameSessionIDs()
	{
		return rxdatabase.select("SELECT id FROM game_session WHERE current_game_id IS NOT NULL;")
				.getAs(Integer.class)
				.compose(resultTransformerQueryFlowable());
	}

	public Completable endGameSession(int gameSessionID)
	{
		return rxdatabase.update("UPDATE game_session SET current_game_id = NULL WHERE id = ?;")
				.parameter(gameSessionID).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Completable deleteGameSession(int gameSessionID)
	{
		return rxdatabase.update("DELETE FROM game_session WHERE id = ?;")
				.parameter(gameSessionID).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Completable addPlayer(int gameSessionID, PlayerSeat seat, User user)
	{
		return rxdatabase.update("INSERT INTO player(game_session_id, seat, user_id) VALUES(?, ?, ?);")
				.parameters(gameSessionID, seat.asInt(), user.getID()).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Flowable<User> getUsersForGameSession(int gameSessionID)
	{
		return rxdatabase.select("SELECT user_id FROM player WHERE game_session_id = ? ORDER BY seat;")
				.parameter(gameSessionID).getAs(Integer.class).map(this::getUser)
				.compose(resultTransformerQueryFlowable());
	}

	public Completable setPlayerPoints(int gameSessionID, PlayerSeat seat, int value)
	{
		return rxdatabase.update("UPDATE player SET points = ? WHERE game_session_id = ? AND seat = ?;")
				.parameters(value, gameSessionID, seat.asInt()).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Flowable<Integer> getPlayerPoints(int gameSessionID)
	{
		return rxdatabase.select("SELECT points FROM player WHERE game_session_id = ? ORDER BY seat;")
				.parameter(gameSessionID).getAs(Integer.class)
				.compose(resultTransformerQueryFlowable());
	}

	public Completable chat(int gameSessionID, int userID, String message)
	{
		return rxdatabase.update("INSERT INTO chat(game_session_id, user_id, message, time) VALUES(?, ?, ?, ?);")
				.parameters(gameSessionID, userID, message, System.currentTimeMillis()).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Flowable<Tuple3<Integer, String, Long>> getChats(int gameSessionID)
	{
		return rxdatabase.select("SELECT user_id, message, time FROM chat WHERE game_session_id = ? ORDER BY time;")
				.parameter(gameSessionID).getAs(Integer.class, String.class, Long.class)
				.compose(resultTransformerQueryFlowable());
	}

	public Single<Integer> createGame(int gameSessionID, PlayerSeat beginnerPlayer)
	{
		return rxdatabase.update("INSERT INTO game(game_session_id, beginner_player, create_time) VALUES(?, ?, ?);")
				.parameters(gameSessionID, beginnerPlayer.asInt(), System.currentTimeMillis())
				.returnGeneratedKeys().getAs(Integer.class).singleOrError()
				.doOnSuccess(gameID ->
						rxdatabase.update("UPDATE game_session SET current_game_id = ? WHERE id = ?;")
						.parameters(gameID, gameSessionID).complete().compose(resultTransformerUpdateCompletable()))
				.compose(resultTransformerUpdateSingle());
	}

	public Single<Tuple3<Integer, PlayerSeat, Long>> getGame(int gameID)
	{
		return rxdatabase.select("SELECT game_session_id, beginner_player, create_time FROM game WHERE id = ?;")
				.parameter(gameID).getAs(Integer.class, Integer.class, Long.class).map(tuple -> Tuple3.create(tuple._1(), PlayerSeat.fromInt(tuple._2()), tuple._3())).singleOrError()
				.compose(resultTransformerQuerySingle());
	}

	public Completable setDeck(int gameID, List<Card> deck)
	{
		Flowable<Object> parameters = Flowable.range(0, deck.size())
				.concatMap(i -> Flowable.just(gameID, i, deck.get(i).getID()));

		return rxdatabase.update("INSERT INTO deck_card(game_id, ordinal, card) VALUES(?, ?, ?);")
				.batchSize(deck.size()).parameterStream(parameters).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Flowable<Card> getDeck(int gameID)
	{
		return rxdatabase.select("SELECT card FROM deck_card WHERE game_id = ? ORDER BY ordinal;")
				.parameter(gameID).getAs(String.class).map(Card::fromId)
				.compose(resultTransformerQueryFlowable());
	}

	public Completable addAction(int gameID, int player, Action action, int ordinal)
	{
		return rxdatabase.update("INSERT INTO action(game_id, ordinal, seat, action, time) VALUES(?, ?, ?, ?, ?);")
				.parameters(gameID, ordinal, player, action.getId(), System.currentTimeMillis()).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	public Flowable<Tuple3<PlayerSeat, Action, Long>> getActions(int gameID)
	{
		return rxdatabase.select("SELECT seat, action, time FROM action WHERE game_id = ? ORDER BY ordinal;")
				.parameter(gameID).getAs(Integer.class, String.class, Long.class)
				.map(tuple -> Tuple3.create(PlayerSeat.fromInt(tuple._1()), new Action(tuple._2()), tuple._3()))
				.compose(resultTransformerQueryFlowable());
	}

	public Completable addFCMToken(String token, User user)
	{
		Completable insert = rxdatabase.update("INSERT INTO fcm_token(token, user_id) VALUES (?, ?);")
				.parameters(token, user.getID()).counts().ignoreElements();

		return rxdatabase.update("UPDATE fcm_token SET user_id = ? WHERE token = ?;")
				.parameters(user.getID(), token).counts().singleOrError()
				.flatMapCompletable(count -> count == 0 ? insert : Completable.complete())
				.compose(resultTransformerUpdateCompletable());
	}

	public Completable removeFCMToken(String token)
	{
		return rxdatabase.update("DELETE FROM fcm_token WHERE token = ?;")
				.parameter(token).complete()
				.compose(resultTransformerUpdateCompletable());
	}

	Flowable<String> getFCMTokensForUser(int userID)
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
