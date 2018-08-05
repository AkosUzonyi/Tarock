package com.tisza.tarock.server;

import org.json.*;
import sun.net.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class FacebookUserManager
{
	private static final String APP_ID = "1735167250066232";

	private Map<String, User> idToUser = new HashMap<>();

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
			if (!appJSON.getString("id").equals(APP_ID))
				return null;

			JSONObject userJSON = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,name,picture,friends&access_token=" + accessToken);

			String id = userJSON.getString("id");
			if (idToUser.containsKey(id))
				return idToUser.get(id);

			String name = userJSON.getString("name");

			String imgURL = null;
			if (userJSON.has("picture"))
				imgURL = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

			List<String> friendIDs = new ArrayList<>();
			if (userJSON.has("friends"))
			{
				for (Object friendJSON : userJSON.getJSONObject("friends").getJSONArray("data"))
				{
					friendIDs.add(((JSONObject)friendJSON).getString("id"));
				}
			}

			User user = new User(id, name, imgURL, friendIDs);
			System.out.println("user created: " + name);
			idToUser.put(id, user);
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
	}
}
