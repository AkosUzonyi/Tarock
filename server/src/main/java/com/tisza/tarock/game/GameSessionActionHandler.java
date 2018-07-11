package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class GameSessionActionHandler implements ActionHandler
{
	private final GameSession gameSession;

	public GameSessionActionHandler(GameSession gameSession)
	{
		this.gameSession = gameSession;
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		gameSession.getCurrentPhase().requestHistory(player);
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcementContra)
	{
		gameSession.getCurrentPhase().announce(player, announcementContra);
	}

	@Override
	public void announcePassz(PlayerSeat player)
	{
		gameSession.getCurrentPhase().announcePassz(player);
	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{
		gameSession.getCurrentPhase().bid(player, bid);
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		gameSession.getCurrentPhase().call(player, card);
	}

	@Override
	public void change(PlayerSeat player, List<Card> cards)
	{
		gameSession.getCurrentPhase().change(player, cards);
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		gameSession.getCurrentPhase().playCard(player, card);
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		gameSession.getCurrentPhase().readyForNewGame(player);
	}

	@Override
	public void throwCards(PlayerSeat player)
	{
		gameSession.getCurrentPhase().throwCards(player);
	}
}
