package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class GameSessionActionHandler implements ActionHandler
{
	private final GameSession gameSession;

	public GameSessionActionHandler(GameSession gameSession)
	{
		this.gameSession = gameSession;
	}

	private ActionHandler getCurrentHandler()
	{
		return gameSession.getCurrentGame().getCurrentPhase();
	}

	@Override
	public void requestHistory(PlayerSeat player, EventSender eventSender)
	{
		getCurrentHandler().requestHistory(player, eventSender);
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcementContra)
	{
		getCurrentHandler().announce(player, announcementContra);
	}

	@Override
	public void announcePassz(PlayerSeat player)
	{
		getCurrentHandler().announcePassz(player);
	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{
		getCurrentHandler().bid(player, bid);
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		getCurrentHandler().call(player, card);
	}

	@Override
	public void change(PlayerSeat player, List<Card> cards)
	{
		getCurrentHandler().change(player, cards);
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		getCurrentHandler().playCard(player, card);
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		getCurrentHandler().readyForNewGame(player);
	}

	@Override
	public void throwCards(PlayerSeat player)
	{
		getCurrentHandler().throwCards(player);
	}
}
