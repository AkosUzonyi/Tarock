package com.tisza.tarock.spring.service;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.spring.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.concurrent.*;

@Service
public class BotService
{
	@Autowired
	private GameService gameService;

	private final ScheduledExecutorService taskScheduler = Executors.newSingleThreadScheduledExecutor();
	private final Random rnd = new Random();

	public void executeBotActions(GameDB gameDB, Game game)
	{
		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			if (!game.getTurn(seat))
				continue;

			PlayerDB playerDB = gameService.getPlayerFromSeat(gameDB, seat);
			if (!playerDB.user.getIsBot())
				continue;

			Action botAction = getAction(game, seat);
			int delay = getDelay(game, seat);
			taskScheduler.schedule(() -> gameService.executeAction(gameDB.id, seat, botAction), delay, TimeUnit.MILLISECONDS);
		}
	}

	private int getDelay(Game game, PlayerSeat bot)
	{
		switch (game.getCurrentPhaseEnum())
		{
			case BIDDING:
			case CALLING:
			case ANNOUNCING:
				return 1000;
			case END:
			case INTERRUPTED:
			case FOLDING:
				return 0;
			case GAMEPLAY:
				Trick currentTrick = game.getTrick(game.getTrickCount() - 1);
				return currentTrick.getFirstCard() == null ? 2500 : 1000;
		}
		throw new RuntimeException();
	}

	private Action getAction(Game game, PlayerSeat bot)
	{
		PlayerCards cards = game.getPlayerCards(bot);
		switch (game.getCurrentPhaseEnum())
		{
			case BIDDING:
			case CALLING:
				return chooseRandom(game.getAvailableActions());
			case ANNOUNCING:
				if (!game.getAvailableActions().isEmpty() && rnd.nextFloat() < 0.3)
					return chooseRandom(game.getAvailableActions());
				else
					return Action.announcePassz();
			case FOLDING:
				List<Card> cardsToSkart = cards.filter(new SkartableCardFilter(game.getGameType())).subList(0, cards.size() - 9);
				return Action.fold(cardsToSkart);
			case GAMEPLAY:
				Trick currentTrick = game.getTrick(game.getTrickCount() - 1);
				return Action.play(chooseRandom(cards.getPlayableCards(currentTrick.getFirstCard())));
			case END:
			case INTERRUPTED:
				return Action.readyForNewGame();
		}
		throw new RuntimeException();
	}

	private <T> T chooseRandom(Collection<T> from)
	{
		int n = rnd.nextInt(from.size());
		Iterator<T> it = from.iterator();
		for (int i = 0; i < n; i++)
		{
			it.next();
		}
		return it.next();
	}
}
