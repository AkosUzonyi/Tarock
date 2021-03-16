package com.tisza.tarock.spring;

import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Component
public class GameSessionCleanup
{
	private static final int MAX_GAME_IDLE_TIME = 2 * 3600 * 1000;

	@Autowired
	private GameSessionRepository gameSessionRepository;
	@Autowired
	private GameRepository gameRepository;

	@Scheduled(fixedRate = 10 * 60 * 1000)
	@Transactional
	public void deleteOldGameSessions()
	{
		for (GameSessionDB gameSession : gameSessionRepository.findActive())
		{
			if (!gameSession.state.equals("game"))
				return;

			Optional<GameDB> game = gameRepository.findById(gameSession.currentGameId);
			if (game.isEmpty())
				continue;

			long lastActionTime = game.get().actions.get(game.get().actions.size() - 1).time;
			if (System.currentTimeMillis() - lastActionTime > MAX_GAME_IDLE_TIME)
				gameSession.state = "deleted";
		}
	}
}
