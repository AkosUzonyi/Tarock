package com.tisza.tarock.server;

import com.google.auth.oauth2.*;
import com.google.firebase.*;
import com.google.firebase.auth.*;
import com.google.firebase.messaging.*;
import org.apache.log4j.*;

import java.io.*;
import java.util.*;

public class FirebaseNotificationSender
{
	private static final Logger log = Logger.getLogger(FirebaseNotificationSender.class);

	private File jsonFile;

	public FirebaseNotificationSender(File jsonFile)
	{
		this.jsonFile = jsonFile;
	}

	public void initialize() throws IOException
	{
		FileInputStream refreshToken = new FileInputStream(jsonFile);

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(refreshToken))
				.setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
				.build();

		FirebaseApp.initializeApp(options);
	}

	public boolean sendNewGameNotification(String token, int gameID, String creatorName, List<String> playerNames)
	{
		AndroidNotification notification = AndroidNotification.builder()
				.setTitleLocalizationKey("game_created_notification_title")
				.addTitleLocalizationArg(creatorName)
				.setBodyLocalizationKey("game_created_notification_body")
				.addAllBodyLocalizationArgs(playerNames)
				.setSound("default")
				.build();

		AndroidConfig androidConfig = AndroidConfig.builder()
				.setNotification(notification)
				.setTtl(60000)
				.putData("game_id", String.valueOf(gameID))
				.build();

		Message message = Message.builder()
				.setAndroidConfig(androidConfig)
				.setToken(token)
				.build();

		try
		{
			FirebaseMessaging.getInstance().send(message);
		}
		catch (FirebaseMessagingException e)
		{
			return false;
		}

		return true;
	}
}
