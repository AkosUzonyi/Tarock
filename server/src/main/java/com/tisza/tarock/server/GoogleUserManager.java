package com.tisza.tarock.server;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.jackson2.*;
import com.tisza.tarock.server.database.*;
import io.reactivex.*;

import java.util.*;

public class GoogleUserManager
{
	private static final String CLIENT_ID = "622849615637-7dth7ktpr1mgk36ol7h7me819vbc8flg.apps.googleusercontent.com";
	private final TarockDatabase database;
	private GoogleIdTokenVerifier tokenVerifier;

	public GoogleUserManager(TarockDatabase database)
	{
		this.database = database;
		tokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
				.setAudience(Collections.singletonList(CLIENT_ID))
				.build();
	}

	public Single<User> newToken(String token)
	{
		try
		{
			System.out.println(token);
			GoogleIdToken idToken = tokenVerifier.verify(token);
			if (idToken == null)
				return Single.error(new Exception("Token verification failed"));

			GoogleIdToken.Payload payload = idToken.getPayload();
			return database.setGoogleUserData(payload.getSubject(), (String)payload.get("name"), (String)payload.get("picture"));
		}
		catch (Exception e)
		{
			return Single.error(new Exception("Error during token verification", e));
		}
	}
}
