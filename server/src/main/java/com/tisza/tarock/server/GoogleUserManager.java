package com.tisza.tarock.server;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.jackson2.*;
import com.tisza.tarock.server.database.*;
import io.reactivex.*;
import io.reactivex.schedulers.*;

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
		return verifyToken(token).flatMap(payload -> database.setGoogleUserData(payload.getSubject(), (String)payload.get("family_name") + " " + (String)payload.get("given_name"), (String)payload.get("picture")));
	}

	private Single<GoogleIdToken.Payload> verifyToken(String token)
	{
		return Single.fromCallable(() ->
		{
			GoogleIdToken idToken = tokenVerifier.verify(token);
			if (idToken == null)
				throw new Exception("Token verification failed");

			return idToken.getPayload();
		})
		.subscribeOn(Schedulers.io());
	}
}
