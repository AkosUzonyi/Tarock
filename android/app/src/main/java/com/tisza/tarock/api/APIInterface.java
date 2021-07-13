package com.tisza.tarock.api;

import com.tisza.tarock.api.model.*;
import io.reactivex.*;
import io.reactivex.Observable;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.*;

public interface APIInterface
{
	@POST("/api/auth/login")
	Single<LoginResponseDTO> login(@Body LoginRequestDTO loginRequestDTO);

	@GET("/api/users/{id}")
	Single<User> getUser(@Path("id") int id);

	@GET("/api/gameSessions")
	Single<List<GameSession>> getGameSessions();

	@POST("/api/gameSessions")
	Single<ResponseBody> createGameSession(@Body CreateGameSessionDTO createGameSessionDTO);

	@GET("/api/gameSessions/{id}")
	Single<GameSession> getGameSession(@Path("id") int id);

	@DELETE("/api/gameSessions/{id}")
	Single<ResponseBody> deleteGameSession(@Path("id") int id);

	@DELETE("/api/gameSessions/{id}/join")
	Single<ResponseBody> joinGameSession(@Path("id") int id);

	@POST("/api/gameSessions/{id}/leave")
	Single<ResponseBody> leaveGameSession(@Path("id") int id);

	@POST("/api/gameSessions/{id}/start")
	Single<ResponseBody> startGameSession(@Path("id") int id);

	@GET("/api/games/{id}")
	Single<GameDTO> getGame(@Path("id") int id);

	@GET("/api/gameSessions/{id}/chat")
	Single<List<Chat>> getChat(@Path("id") int gameSessionId, @Query("from") long from);

	@POST("/api/gameSessions/{id}/chat")
	Single<ResponseBody> postChat(@Path("id") int gameSessionId, @Body ChatPostDTO chatPostDTO);

	@GET("/api/games/{id}/actions")
	Single<List<Action>> getActions(@Path("id") int gameId, @Query("from") int from);

	@POST("/api/games/{id}/actions")
	Single<ResponseBody> postAction(@Path("id") int gameId, @Body ActionPostDTO actionPostDTO);

	@GET("/api/games/{id}")
	Single<GameStateDTO> getGameState(@Path("id") int id);
}
