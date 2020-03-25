package com.tisza.tarock.server;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.jackson2.*;
import com.tisza.tarock.server.database.*;
import io.reactivex.*;

import java.util.*;

public class GoogleUserManager
{
	private static final String[] CLIENT_IDS = {"622849615637-ckunoefd465cq1072h0okal6qlbun0uq.apps.googleusercontent.com", "622849615637-3ibqdgqen3k7bmpt9lnrhqbik47nkb5h.apps.googleusercontent.com", "622849615637-7dth7ktpr1mgk36ol7h7me819vbc8flg.apps.googleusercontent.com", "622849615637-hn7peq0o1ofbs1p9oicum1ggiclmj5mb.apps.googleusercontent.com"};
	private final TarockDatabase database;
	private GoogleIdTokenVerifier tokenVerifier;

	public GoogleUserManager(TarockDatabase database)
	{
		this.database = database;
		tokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
				.setAudience(Arrays.asList(CLIENT_IDS))
				.build();
	}

	public Single<User> newToken(String token)
	{
		try
		{
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
