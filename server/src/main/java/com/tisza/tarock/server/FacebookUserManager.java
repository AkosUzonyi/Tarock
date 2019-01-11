package com.tisza.tarock.server;

import org.json.*;
import sun.net.util.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class FacebookUserManager
{
	private static final String APP_ID = "1735167250066232";
	private static final String DATABASE_FILENAME = "fbusers.db";

	private final String dbURL;
	private Connection databaseConnection;
	private Map<String, User> idToUser = new HashMap<>();
	private Map<String, User> fcmTokenToUser = new HashMap<>();

	public FacebookUserManager(File dbDir)
	{
		dbURL = "jdbc:sqlite:" + new File(dbDir, DATABASE_FILENAME).getAbsolutePath();
	}

	public void initialize() throws SQLException
	{
		if (databaseConnection != null)
			throw new IllegalStateException();
		
		databaseConnection = DriverManager.getConnection(dbURL);
		Statement statement;
		ResultSet resultSet;

		statement = databaseConnection.createStatement();
		statement.execute("create table if not exists users (id varchar(255) primary key, name varchar(255), imgURL varchar(255), registration_time int);");
		statement.execute("create table if not exists friendships (id0 varchar(255), id1 varchar(255), primary key (id0, id1));");
		statement.execute("create table if not exists fcm_token (token varchar(255) primary key, user_id varchar(255));");
		statement.execute("select id, name, imgURL from users;");
		resultSet = statement.getResultSet();
		while (resultSet.next())
		{
			addUser(resultSet.getString("id"), resultSet.getString("name"), resultSet.getString("imgURL"));
		}
		statement.execute("select id0, id1 from friendships;");
		resultSet = statement.getResultSet();
		while (resultSet.next())
		{
			User user0 = getUserByID(resultSet.getString("id0"));
			User user1 = getUserByID(resultSet.getString("id1"));
			user0.addFriend(user1);
			user1.addFriend(user0);
		}
		statement.close();
		statement.execute("select token, user_id from fcm_token;");
		resultSet = statement.getResultSet();
		while (resultSet.next())
		{
			String token = resultSet.getString("token");
			User user = getUserByID(resultSet.getString("user_id"));
			fcmTokenToUser.put(token, user);
			user.addFCMToken(token);
		}
		statement.close();
	}

	private User addUser(String id, String name, String imgURL)
	{
		idToUser.putIfAbsent(id, new User(id));
		User user = idToUser.get(id);
		user.setName(name);
		user.setImgURL(imgURL);
		return user;
	}

	public Collection<User> listUsers()
	{
		return idToUser.values();
	}

	public User getUserByID(String id)
	{
		return idToUser.get(id);
	}

	public void registerFCMToken(String token, User user)
	{
		PreparedStatement preparedStatement;

		try
		{
			System.out.println("fcm token for: " + (user == null ? null : user.getName()));

			preparedStatement = databaseConnection.prepareStatement("delete from fcm_token where token=?;");
			preparedStatement.setString(1, token);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			User oldUser = fcmTokenToUser.remove(token);
			if (oldUser != null)
				oldUser.removeFCMToken(token);

			if (user != null)
			{
				preparedStatement = databaseConnection.prepareStatement("insert into fcm_token (token, user_id) values (?, ?);");
				preparedStatement.setString(1, token);
				preparedStatement.setString(2, user.getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();
				fcmTokenToUser.put(token, user);
				user.addFCMToken(token);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	public User getUserByAccessToken(String accessToken)
	{
		PreparedStatement preparedStatement;

		try
		{
			JSONObject appJSON = downloadJSONFromURL("https://graph.facebook.com/app/?access_token=" + accessToken);
			if (!appJSON.getString("id").equals(APP_ID))

				return null;

			JSONObject userJSON = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,name,picture,friends&access_token=" + accessToken);
			String id = userJSON.getString("id");
			String name = userJSON.getString("name");
			String imgURL = null;
			if (userJSON.has("picture"))
				imgURL = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

			User newUser = addUser(id, name, imgURL);

			for (User user : listUsers())
			{
				user.removeFriend(newUser);
			}

			preparedStatement = databaseConnection.prepareStatement("delete from friendships where id0=? or id1=?;");
			preparedStatement.setString(1, newUser.getId());
			preparedStatement.setString(2, newUser.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();

			if (userJSON.has("friends"))
			{
				newUser.clearFriends();
				for (Object friendJSON : userJSON.getJSONObject("friends").getJSONArray("data"))
				{
					String friendID = ((JSONObject)friendJSON).getString("id");
					User friendUser = getUserByID(friendID);
					if (friendUser == null)
						continue;

					newUser.addFriend(friendUser);
					friendUser.addFriend(newUser);

					preparedStatement = databaseConnection.prepareStatement("insert or ignore into friendships(id0, id1) values (?, ?);");
					preparedStatement.setString(1, id);
					preparedStatement.setString(2, friendID);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}

			preparedStatement = databaseConnection.prepareStatement("update users set name=?, imgURL=? where id=?;");
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, imgURL);
			preparedStatement.setString(3, id);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = databaseConnection.prepareStatement("insert or ignore into users(id, name, imgURL, registration_time) values (?, ?, ?, ?);");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, name);
			preparedStatement.setString(3, imgURL);
			preparedStatement.setLong(4, System.currentTimeMillis());
			preparedStatement.executeUpdate();
			preparedStatement.close();

			return newUser;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject downloadJSONFromURL(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setReadTimeout(1000);

		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		StringBuffer response = new StringBuffer();

		String inputLine;
		while ((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();

		return new JSONObject(response.toString());
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
