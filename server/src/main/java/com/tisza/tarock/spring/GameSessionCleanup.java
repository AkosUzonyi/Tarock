package com.tisza.tarock.spring;

import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import com.tisza.tarock.spring.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Component
public class GameSessionCleanup
{
	private static final int MAX_GAME_IDLE_TIME = 2 * 3600 * 1000;

	private final GameSessionRepository gameSessionRepository;
	private final GameRepository gameRepository;
	private final GameSessionService gameSessionService;

	public GameSessionCleanup(GameSessionRepository gameSessionRepository, GameRepository gameRepository, GameSessionService gameSessionService)
	{
		this.gameSessionRepository = gameSessionRepository;
		this.gameRepository = gameRepository;
		this.gameSessionService = gameSessionService;
	}

	@Scheduled(fixedRate = 10 * 60 * 1000)
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void deleteOldGameSessions()
	{
		for (GameSessionDB gameSession : gameSessionRepository.findActive())
		{
			if (gameSession.state.equals("deleted"))
				continue;

			long lastModified = gameSession.createTime;
			if (gameSession.currentGameId != null)
			{
				Optional<GameDB> game = gameRepository.findById(gameSession.currentGameId);
				if (game.isPresent()) {
					List<ActionDB> actions = game.get().actions;
					if (!actions.isEmpty())
						lastModified = actions.get(actions.size() - 1).time;
				}
			}

			if (System.currentTimeMillis() - lastModified > MAX_GAME_IDLE_TIME)
				gameSessionService.deleteGameSession(gameSession.id);
		}
	}
}
