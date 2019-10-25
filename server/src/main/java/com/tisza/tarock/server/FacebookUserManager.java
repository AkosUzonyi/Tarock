package com.tisza.tarock.server;

import io.reactivex.*;
import io.reactivex.schedulers.*;
import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class FacebookUserManager
{
	private static final String APP_ID = "1735167250066232";
	private final Database database;

	public FacebookUserManager(Database database)
	{
		this.database = database;
	}

	public Single<User> newAccessToken(String accessToken)
	{
		Single<JSONObject> appJSONObservable = downloadJSONFromURL("https://graph.facebook.com/app/?access_token=" + accessToken);
		Single<JSONObject> userJSONObservable = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,name,picture,friends&access_token=" + accessToken);

		return Single.merge(Single.zip(appJSONObservable, userJSONObservable, (appJSON, userJSON) ->
		{
			if (!appJSON.getString("id").equals(APP_ID))
				return Single.error(new Exception("wrong app id"));

			String id = userJSON.getString("id");
			String name = userJSON.getString("name");
			String imgURL = null;
			if (userJSON.has("picture"))
				imgURL = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

			List<String> friendFacebookIDs = new ArrayList<>();
			if (userJSON.has("friends"))
			{
				for (Object friendJSON : userJSON.getJSONObject("friends").getJSONArray("data"))
					friendFacebookIDs.add(((JSONObject)friendJSON).getString("id"));
			}

			return database.setFacebookUserData(id, name, imgURL, friendFacebookIDs).map(database::getUser);
		}));
	}

	private Single<JSONObject> downloadJSONFromURL(String urlString)
	{
		return Single.<JSONObject>create(subscriber ->
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

			subscriber.onSuccess(new JSONObject(response.toString()));
		})
		.subscribeOn(Schedulers.io());
	}
}
