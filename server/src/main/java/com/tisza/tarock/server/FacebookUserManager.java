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
		statement.execute("create table if not exists users (id varchar(255) primary key, name varchar(255), imgURL varchar(255));");
		statement.execute("create table if not exists friendships (id0 varchar(255), id1 varchar(255), primary key (id0, id1));");
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

	public User getUserByAccessToken(String accessToken)
	{
		try
		{
			JSONObject appJSON = downloadJSONFromURL("https://graph.facebook.com/app/?access_token=" + accessToken);
			/*if (!appJSON.getString("id").equals(APP_ID))
				return null;*/

			JSONObject userJSON = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,name,picture,friends&access_token=" + accessToken);
			String id = userJSON.getString("id");
			String name = userJSON.getString("name");
			String imgURL = null;
			if (userJSON.has("picture"))
				imgURL = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

			User user = addUser(id, name, imgURL);

			if (userJSON.has("friends"))
			{
				user.clearFriends();
				for (Object friendJSON : userJSON.getJSONObject("friends").getJSONArray("data"))
				{
					String friendID = ((JSONObject)friendJSON).getString("id");
					User friendUser = getUserByID(friendID);
					if (friendUser == null)
						continue;

					user.addFriend(friendUser);
					friendUser.addFriend(user);

					PreparedStatement preparedStatement = databaseConnection.prepareStatement("delete from friendships where id0=? and id1=?;");
					preparedStatement.setString(1, id);
					preparedStatement.setString(2, friendID);
					preparedStatement.executeUpdate();
					preparedStatement.close();

					preparedStatement = databaseConnection.prepareStatement("insert into friendships(id0, id1) values (?, ?);");
					preparedStatement.setString(1, id);
					preparedStatement.setString(2, friendID);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}

			PreparedStatement preparedStatement = databaseConnection.prepareStatement("delete from users where id=?;");
			preparedStatement.setString(1, id);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			preparedStatement = databaseConnection.prepareStatement("insert into users(id, name, imgURL) values (?, ?, ?);");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, name);
			preparedStatement.setString(3, imgURL);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			System.out.println("user created: " + name);

			return user;
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
