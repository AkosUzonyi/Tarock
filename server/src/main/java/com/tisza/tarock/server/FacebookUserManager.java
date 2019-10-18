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
	private final Database database;

	public FacebookUserManager(Database database)
	{
		this.database = database;
	}

	public int newAccessToken(String accessToken)
	{
		try
		{
			JSONObject appJSON = downloadJSONFromURL("https://graph.facebook.com/app/?access_token=" + accessToken);
			if (!appJSON.getString("id").equals(APP_ID))
				return -1;

			JSONObject userJSON = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,name,picture,friends&access_token=" + accessToken);
			String id = userJSON.getString("id");
			String name = userJSON.getString("name");
			String imgURL = null;
			if (userJSON.has("picture"))
				imgURL = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

			Collection<String> friendFacebookIDs = new ArrayList<>();
			if (userJSON.has("friends"))
			{
				for (Object friendJSON : userJSON.getJSONObject("friends").getJSONArray("data"))
					friendFacebookIDs.add(((JSONObject)friendJSON).getString("id"));
			}

			return database.setFacebookUserData(id, name, imgURL, friendFacebookIDs);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
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
}
