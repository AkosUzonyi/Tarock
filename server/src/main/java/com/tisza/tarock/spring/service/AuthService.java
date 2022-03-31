package com.tisza.tarock.spring.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.*;
import com.google.api.client.json.jackson2.*;
import com.tisza.tarock.spring.*;
import com.tisza.tarock.spring.dto.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import org.json.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

@Service
public class AuthService
{
	private static final String FACEBOOK_APP_ID = "1735167250066232";
	private static final String GOOGLE_CLIENT_ID = "622849615637-7dth7ktpr1mgk36ol7h7me819vbc8flg.apps.googleusercontent.com";
	private final GoogleIdTokenVerifier googleIdTokenVerifier =
			new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
					.setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
					.build();

	private final UserRepository userRepository;
	private final IdpUserRepository idpUserRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(UserRepository userRepository, IdpUserRepository idpUserRepository, JwtTokenProvider jwtTokenProvider)
	{
		this.userRepository = userRepository;
		this.idpUserRepository = idpUserRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public LoginResponseDTO auth(String idpServiceId, String idpToken) throws Exception
	{
		UserDB userDB;
		switch (idpServiceId)
		{
			case "facebook":
				userDB = facebookAuth(idpToken);
				break;
			case "google":
				userDB = googleAuth(idpToken);
				break;
			default:
				throw new IllegalArgumentException("invalid idpServiceId: " + idpServiceId);
		}

		LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
		loginResponseDTO.token = jwtTokenProvider.createToken(userDB.id);
		loginResponseDTO.user = userDB;
		return loginResponseDTO;
	}

	private UserDB genericAuth(String idpServiceId, String idpUserId, String name, String imgUrl)
	{
		IdpUserDB idpUser = idpUserRepository.findByIdpServiceIdAndIdpUserId(idpServiceId, idpUserId);
		UserDB userDB;

		if (idpUser == null)
		{
			userDB = new UserDB();
			userDB.name = name;
			userDB.imgUrl = imgUrl;
			userDB.registrationTime = System.currentTimeMillis();
			userDB = userRepository.save(userDB);

			idpUser = new IdpUserDB();
			idpUser.idpUserId = idpUserId;
			idpUser.idpServiceId = idpServiceId;
			idpUser.userId = userDB.id;
			idpUserRepository.save(idpUser);
		}
		else
		{
			userDB = userRepository.findById(idpUser.userId).orElseThrow();
			userDB.name = name;
			userDB.imgUrl = imgUrl;
		}

		return userDB;
	}

	private UserDB facebookAuth(String accessToken) throws Exception
	{
		String accessTokenEncoded = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

		JSONObject appJSON = downloadJSONFromURL("https://graph.facebook.com/app/?access_token=" + accessTokenEncoded);
		if (!appJSON.getString("id").equals(FACEBOOK_APP_ID))
			throw new Exception("wrong app id");

		JSONObject userJSON = downloadJSONFromURL("https://graph.facebook.com/me/?fields=id,last_name,first_name,picture.type(normal),friends&access_token=" + accessTokenEncoded);
		String id = userJSON.getString("id");
		String name = userJSON.getString("last_name") + " " + userJSON.getString("first_name");
		String imgUrl = null;
		if (userJSON.has("picture"))
			imgUrl = userJSON.getJSONObject("picture").getJSONObject("data").getString("url");

		return genericAuth("facebook", id, name, imgUrl);
	}

	private UserDB googleAuth(String token) throws Exception
	{
		GoogleIdToken idToken = googleIdTokenVerifier.verify(token);
		if (idToken == null)
			throw new Exception("GoogleIdToken verification failed");

		GoogleIdToken.Payload payload = idToken.getPayload();
		String id = payload.getSubject();
		String name = (String)payload.get("family_name") + " " + (String)payload.get("given_name");
		String imgUrl = (String)payload.get("picture");

		return genericAuth("google", id, name, imgUrl);
	}

	private JSONObject downloadJSONFromURL(String urlString) throws IOException
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
	}
}
