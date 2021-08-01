package com.tisza.tarock.api;

import com.tisza.tarock.api.model.*;
import io.reactivex.*;
import io.reactivex.Observable;
import okhttp3.*;
import retrofit2.http.*;

import java.util.*;

public interface APIInterface
{
	@POST("/api/auth/login")
	Observable<LoginResponseDTO> login(@Body LoginRequestDTO loginRequestDTO);

	@GET("/api/users/{id}")
	Observable<User> getUser(@Path("id") int id);

	@GET("/api/gameSessions")
	Observable<List<GameSession>> getGameSessions();

	@POST("/api/gameSessions")
	Observable<ResponseBody> createGameSession(@Body CreateGameSessionDTO createGameSessionDTO);

	@GET("/api/gameSessions/{id}")
	Observable<GameSession> getGameSession(@Path("id") int id);

	@DELETE("/api/gameSessions/{id}")
	Observable<ResponseBody> deleteGameSession(@Path("id") int id);

	@POST("/api/gameSessions/{id}/join")
	Observable<ResponseBody> joinGameSession(@Path("id") int id);

	@POST("/api/gameSessions/{id}/leave")
	Observable<ResponseBody> leaveGameSession(@Path("id") int id);

	@POST("/api/gameSessions/{id}/start")
	Observable<ResponseBody> startGameSession(@Path("id") int id);

	@GET("/api/games/{id}")
	Observable<GameDTO> getGame(@Path("id") int id);

	@GET("/api/gameSessions/{id}/chat")
	Observable<List<Chat>> getChat(@Path("id") int gameSessionId, @Query("from") long from);

	@POST("/api/gameSessions/{id}/chat")
	Observable<ResponseBody> postChat(@Path("id") int gameSessionId, @Body ChatPostDTO chatPostDTO);

	@GET("/api/games/{id}/actions")
	Observable<List<ActionDTO>> getActions(@Path("id") int gameId, @Query("from") int from);

	@POST("/api/games/{id}/actions")
	Observable<ResponseBody> postAction(@Path("id") int gameId, @Body ActionPostDTO actionPostDTO);

	@GET("/api/games/{id}/state")
	Observable<GameStateDTO> getGameState(@Path("id") int id);
}
