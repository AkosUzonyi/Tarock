package com.tisza.tarock.server;

import com.tisza.tarock.server.database.*;
import io.reactivex.*;
import io.reactivex.schedulers.*;
import org.json.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

public class FacebookUserManager
{
	private static final String APP_ID = "1735167250066232";
	private final TarockDatabase database;

	public FacebookUserManager(TarockDatabase database)
	{
		this.database = database;
	}

	public void refreshImageURLs()
	{
		database.getFacebookUsers().subscribe(tuple ->
		{
			String facebookID = tuple._1();
			User user = tuple._2();
			getRedirectURLLocation("https://graph.facebook.com/" + facebookID + "/picture?type=normal").subscribe(user::setImageURL);
		});
	}

	public Single<User> newAccessToken(String accessToken)
	{
		String accessTokenEncoded = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
		Single<JSONObject> appJSONSingle = downloadJSONFromURL("https://graph.facebook.com/app/?access_token=" + accessTokenEncoded);
		Single<JSONObject> userJSONSingle = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,last_name,first_name,picture.type(normal),friends&access_token=" + accessTokenEncoded);

		return Single.merge(Single.zip(appJSONSingle, userJSONSingle, (appJSON, userJSON) ->
		{
			if (!appJSON.getString("id").equals(APP_ID))
				return Single.error(new Exception("wrong app id"));

			String id = userJSON.getString("id");
			String name = userJSON.getString("last_name") + " " + userJSON.getString("first_name");
			String imgURL = null;
			if (userJSON.has("picture"))
				imgURL = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

			List<String> friendFacebookIDs = new ArrayList<>();
			if (userJSON.has("friends"))
			{
				for (Object friendJSON : userJSON.getJSONObject("friends").getJSONArray("data"))
					friendFacebookIDs.add(((JSONObject)friendJSON).getString("id"));
			}

			return database.setFacebookUserData(id, name, imgURL, friendFacebookIDs);
		}));
	}

	private Single<JSONObject> downloadJSONFromURL(String urlString)
	{
		return Single.fromCallable(() ->
		{
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(1000);

			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			StringBuilder response = new StringBuilder();

			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();

			return new JSONObject(response.toString());
		})
		.subscribeOn(Schedulers.io());
	}

	private Single<String> getRedirectURLLocation(String urlString)
	{
		return Single.fromCallable(() ->
		{
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setInstanceFollowRedirects(false);
			urlConnection.setReadTimeout(1000);
			return urlConnection.getHeaderField("Location");
		})
		.subscribeOn(Schedulers.io());
	}
}
