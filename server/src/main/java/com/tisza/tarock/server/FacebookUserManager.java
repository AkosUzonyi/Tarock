package com.tisza.tarock.server;

import org.json.*;
import sun.net.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class FacebookUserManager
{
	private static Map<String, User> idToUser = new HashMap<>();

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
		User user = createUserFromToken(accessToken);

		if (user == null)
			return null;

		if (idToUser.containsKey(user.getId()))
			return idToUser.get(user.getId());

		idToUser.put(user.getId(), user);
		return user;
	}

	private User createUserFromToken(String accessToken)
	{
		try
		{
			URL url = new URL("https://graph.facebook.com/me/?fields=id,name,picture,friends&access_token=" + accessToken);
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

			JSONObject json = new JSONObject(response.toString());
			String id = json.getString("id");
			String name = json.getString("name");
			String imgURL = json.getJSONObject("picture").getJSONObject("data").getString("url");

			List<String> friendIDs = new ArrayList<>();
			if (json.has("friends"))
			{
				for (Object friendJSON : json.getJSONObject("friends").getJSONArray("data"))
				{
					friendIDs.add(((JSONObject)friendJSON).getString("id"));
				}
			}

			System.out.println("user created: " + name);

			return new User(id, name, imgURL, friendIDs);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void shutdown()
	{
	}
}
