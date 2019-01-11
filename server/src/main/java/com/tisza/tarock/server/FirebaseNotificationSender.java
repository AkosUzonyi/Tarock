package com.tisza.tarock.server;

import com.google.api.client.googleapis.auth.oauth2.*;
import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class FirebaseNotificationSender
{
	private static final String PROJECT_ID = "tarokk-3f659";
	private File jsonFile;
	private String accessToken = null;

	public FirebaseNotificationSender(File jsonFile)
	{
		this.jsonFile = jsonFile;
	}

	public boolean sendNewGameNotification(String token, String creatorName, List<String> playerNames) throws IOException
	{
		JSONObject notificationJSON = new JSONObject();
		notificationJSON.put("sound", "default");
		notificationJSON.put("title_loc_key", "game_created_notification_title");
		notificationJSON.put("title_loc_args", Arrays.asList(creatorName));
		notificationJSON.put("body_loc_key", "game_created_notification_body");
		notificationJSON.put("body_loc_args", playerNames);

		JSONObject androidJSON = new JSONObject();
		androidJSON.put("ttl", "60s");
		androidJSON.put("notification", notificationJSON);

		JSONObject messageJSON = new JSONObject();
		messageJSON.put("android", androidJSON);
		messageJSON.put("token", token);

		JSONObject json = new JSONObject();
		json.put("message", messageJSON);

		if (accessToken == null)
			refreshAccessToken();

		String error = doHTTPRequest(json);

		if (error == null)
			return true;

		switch (error)
		{
			case "UNAUTHENTICATED":
				refreshAccessToken();
				error = doHTTPRequest(json);
				break;
			case "NOT_FOUND":
				return false;
		}

		if (error == null)
			return true;

		throw new IOException("Firebase cloud messaging error: " + error);
	}

	private String doHTTPRequest(JSONObject json) throws IOException
	{
		String error = null;

		HttpURLConnection httpURLConnection = (HttpURLConnection)new URL("https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send").openConnection();
		httpURLConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
		httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
		json.write(outputStreamWriter);
		outputStreamWriter.close();

		if (httpURLConnection.getResponseCode() >= 400)
		{
			try
			{
				JSONTokener errorJsonTokener = new JSONTokener(httpURLConnection.getErrorStream());
				JSONObject errorJson = new JSONObject(errorJsonTokener);
				error = errorJson.getJSONObject("error").getString("status");
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				error = "JSON_ERROR";
			}
		}

		return error;
	}

	private void refreshAccessToken() throws IOException
	{
		GoogleCredential googleCredential = GoogleCredential
				.fromStream(new FileInputStream(jsonFile))
				.createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
		googleCredential.refreshToken();
		accessToken = googleCredential.getAccessToken();
	}
}
