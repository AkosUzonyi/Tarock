package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.message.*;
import org.flywaydb.core.*;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Database
{
	private static final String DATABASE_FILENAME = "tarock.db";

	private final String dbURL;
	private Connection databaseConnection;

	public Database(File dbDir)
	{
		dbURL = "jdbc:sqlite:" + new File(dbDir, DATABASE_FILENAME).getAbsolutePath();
	}

	public void initialize() throws SQLException
	{
		if (databaseConnection != null)
			throw new IllegalStateException();

		Flyway flyway = Flyway.configure().dataSource(dbURL, null, null).load();
		flyway.migrate();

		databaseConnection = DriverManager.getConnection(dbURL);

		Statement statement = databaseConnection.createStatement();
		statement.execute("PRAGMA foreign_keys = ON;");
		statement.close();
	}

	public int setFacebookUserData(String facebookId, String name, String imgURL, Collection<String> friendFacebookIDs)
	{
		int userID = -1;

		try
		{
			PreparedStatement preparedStatement;

			preparedStatement = databaseConnection.prepareStatement("UPDATE user SET name = ?, img_url = ? where facebook_id = ?");
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, imgURL);
			preparedStatement.setString(3, facebookId);
			int count = preparedStatement.executeUpdate();
			preparedStatement.close();

			if (count > 0)
			{
				preparedStatement = databaseConnection.prepareStatement("SELECT id FROM user WHERE facebook_id = ?");
				preparedStatement.setString(1, facebookId);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				userID = resultSet.getInt("id");
				preparedStatement.close();
			}
			else
			{
				preparedStatement = databaseConnection.prepareStatement("INSERT INTO user(facebook_id, name, img_url, registration_time) VALUES (?, ?, ?, ?);");
				preparedStatement.setString(1, facebookId);
				preparedStatement.setString(2, name);
				preparedStatement.setString(3, imgURL);
				preparedStatement.setLong(4, System.currentTimeMillis());
				preparedStatement.executeUpdate();

				ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
				if (!generatedKeys.next())
					return userID;

				userID = generatedKeys.getInt(1);
				System.out.println("userid: " + userID);
				preparedStatement.close();
			}

			if (friendFacebookIDs != null)
			{
				preparedStatement = databaseConnection.prepareStatement("DELETE FROM friendship WHERE id0 = ? OR id1 = ?;");
				preparedStatement.setInt(1, userID);
				preparedStatement.setInt(2, userID);
				preparedStatement.executeUpdate();
				preparedStatement.close();

				preparedStatement = databaseConnection.prepareStatement("INSERT INTO friendship(id0, id1) VALUES(?, (SELECT id FROM user WHERE facebook_id = ?));");
				for (String friendFacebookID : friendFacebookIDs)
				{
					preparedStatement.setInt(1, userID);
					preparedStatement.setString(2, friendFacebookID);
					preparedStatement.addBatch();
				}
				preparedStatement.executeBatch();
				preparedStatement.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return userID;
	}

	public User getUser(int userID)
	{
		User user = null;

		try
		{
			PreparedStatement preparedStatement;
			ResultSet resultSet;

			preparedStatement = databaseConnection.prepareStatement("SELECT id FROM user WHERE id = ?;");
			preparedStatement.setInt(1, userID);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next())
				user = new User(userID, this);

			preparedStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return user;
	}

	public String getUserName(int userID)
	{
		String name = null;

		try
		{
			PreparedStatement preparedStatement;
			ResultSet resultSet;

			preparedStatement = databaseConnection.prepareStatement("SELECT name FROM user WHERE id = ?;");
			preparedStatement.setInt(1, userID);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next())
				name = resultSet.getString("name");

			preparedStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return name;
	}

	public String getUserImgURL(int userID)
	{
		String imgURL = null;

		try
		{
			PreparedStatement preparedStatement;
			ResultSet resultSet;

			preparedStatement = databaseConnection.prepareStatement("SELECT img_url FROM user WHERE id = ?;");
			preparedStatement.setInt(1, userID);
			resultSet = preparedStatement.executeQuery();

			if (resultSet.next())
				imgURL = resultSet.getString("img_url");

			preparedStatement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return imgURL;
	}

	public boolean areUserFriends(int userID0, int userID1)
	{
		try
		{
			PreparedStatement preparedStatement;
			ResultSet resultSet;

			preparedStatement = databaseConnection.prepareStatement("SELECT id0 as id FROM friendship WHERE id0 = ? AND id1 = ? UNION SELECT id0 as id FROM friendship WHERE id0 = ? AND id1 = ?;");
			preparedStatement.setInt(1, userID0);
			preparedStatement.setInt(2, userID1);
			preparedStatement.setInt(3, userID1);
			preparedStatement.setInt(4, userID0);
			resultSet = preparedStatement.executeQuery();
			boolean friends = resultSet.next();
			preparedStatement.close();
			return friends;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	//TODO remove
	public List<User> listUsers()
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT id FROM user ORDER BY id;");
			ResultSet resultSet = preparedStatement.executeQuery();
			List<User> result = new ArrayList<>();
			while (resultSet.next())
				result.add(getUser(resultSet.getInt("id")));
			preparedStatement.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public int createGameSession(GameType gameType, DoubleRoundType doubleRoundType)
	{
		try
		{
			PreparedStatement preparedStatement;

			preparedStatement = databaseConnection.prepareStatement("INSERT INTO game_session(type, double_round_type, create_time) VALUES(?, ?, ?);");
			preparedStatement.setString(1, gameType.getID());
			preparedStatement.setString(2, doubleRoundType.getID());
			preparedStatement.setLong(3, System.currentTimeMillis());
			preparedStatement.executeUpdate();

			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
			if (!generatedKeys.next())
				return -1;

			int gameID = generatedKeys.getInt(1);
			preparedStatement.close();

			return gameID;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public List<Integer> getActiveGameSessionIDs()
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT id FROM game_session WHERE current_game_id IS NOT NULL;");
			ResultSet resultSet = preparedStatement.executeQuery();
			List<Integer> result = new ArrayList<>();
			while (resultSet.next())
				result.add(resultSet.getInt("id"));
			preparedStatement.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void stopGameSession(int gameSessionID)
	{
		try
		{
			PreparedStatement preparedStatement;
			preparedStatement = databaseConnection.prepareStatement("UPDATE game_session SET current_game_id = NULL where id = ?;");
			preparedStatement.setInt(1, gameSessionID);
			preparedStatement.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addBotPlayer(int gameSessionID, int seat)
	{
		addUserPlayer(gameSessionID, seat, -1);
	}

	public void addUserPlayer(int gameSessionID, int seat, Integer userID)
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO player(game_session_id, seat, user_id) VALUES(?, ?, ?);");
			preparedStatement.setInt(1, gameSessionID);
			preparedStatement.setInt(2, seat);
			if (userID < 0)
				preparedStatement.setNull(3, Types.INTEGER);
			else
				preparedStatement.setInt(3, userID);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setPlayerPoints(int gameSessionID, int seat, int value)
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("UPDATE player SET points = ? WHERE game_session_id = ? AND seat = ?;");
			preparedStatement.setInt(1, value);
			preparedStatement.setInt(2, gameSessionID);
			preparedStatement.setInt(3, seat);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public int createGame(int gameSessionID, PlayerSeat beginnerPlayer)
	{
		try
		{
			PreparedStatement preparedStatement;

			preparedStatement = databaseConnection.prepareStatement("INSERT INTO game(game_session_id, beginner_player, create_time) VALUES(?, ?, ?);");
			preparedStatement.setInt(1, gameSessionID);
			preparedStatement.setInt(2, beginnerPlayer.asInt());
			preparedStatement.setLong(3, System.currentTimeMillis());
			preparedStatement.executeUpdate();

			ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
			if (!generatedKeys.next())
				return -1;

			int gameID = generatedKeys.getInt(1);
			preparedStatement.close();

			preparedStatement = databaseConnection.prepareStatement("UPDATE game_session SET current_game_id = ? where id = ?;");
			preparedStatement.setInt(1, gameID);
			preparedStatement.setInt(2, gameSessionID);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			return gameID;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setDeck(int gameID, List<Card> deck)
	{
		try
		{
			databaseConnection.setAutoCommit(false);
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO deck_card(game_id, ordinal, card) VALUES(?, ?, ?);");
			int ordinal = 0;
			for (Card card : deck)
			{
				preparedStatement.setInt(1, gameID);
				preparedStatement.setInt(2, ordinal++);
				preparedStatement.setString(3, card.getID());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			preparedStatement.close();
			databaseConnection.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public List<Card> getDeck(int gameID)
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT card FROM deck_card WHERE game_id = ? ORDER BY ordinal;");
			preparedStatement.setInt(1, gameID);
			ResultSet resultSet = preparedStatement.executeQuery();
			List<Card> result = new ArrayList<>();
			while (resultSet.next())
				result.add(Card.fromId(resultSet.getString("card")));
			preparedStatement.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addAction(int gameID, Action action)
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("INSERT INTO action(game_id, ordinal, seat, action, time) VALUES(?, (SELECT MAX(ordinal) from action) + 1, ?, ?, ?);");
			preparedStatement.setInt(1, gameID);
			preparedStatement.setInt(2, action.getPlayer().asInt());
			preparedStatement.setString(3, action.getId());
			preparedStatement.setLong(4, System.currentTimeMillis());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public List<Action> getActions(int gameID)
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT seat, action FROM action WHERE game_id = ? ORDER BY ordinal;");
			preparedStatement.setInt(1, gameID);
			ResultSet resultSet = preparedStatement.executeQuery();
			List<Action> result = new ArrayList<>();
			while (resultSet.next())
				result.add(new Action(PlayerSeat.fromInt(resultSet.getInt("seat")), resultSet.getString("action")));
			preparedStatement.close();
			return result;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addFCMToken(String token, int userID)
	{
		try
		{
			PreparedStatement preparedStatement;

			preparedStatement = databaseConnection.prepareStatement("UPDATE fcm_token SET user_id = ? WHERE token = ?;");
			preparedStatement.setInt(1, userID);
			preparedStatement.setString(2, token);
			int count = preparedStatement.executeUpdate();
			preparedStatement.close();

			if (count > 0)
				return;

			preparedStatement = databaseConnection.prepareStatement("INSERT INTO fcm_token(token, user_id) VALUES (?, ?);");
			preparedStatement.setString(1, token);
			preparedStatement.setInt(2, userID);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void removeFCMToken(String token)
	{
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("DELETE FROM fcm_token WHERE token = ?;");
			preparedStatement.setString(1, token);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Collection<String> getFCMTokensForUser(int userID)
	{
		Collection<String> result = new ArrayList<>();
		try
		{
			PreparedStatement preparedStatement = databaseConnection.prepareStatement("SELECT token FROM fcm_token WHERE user_id = ?;");
			preparedStatement.setInt(1, userID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
				result.add(resultSet.getString("token"));
			preparedStatement.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		return result;
	}

	public void shutdown()
	{
		try
		{
			if (databaseConnection != null)
				databaseConnection.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		databaseConnection = null;
	}
}
