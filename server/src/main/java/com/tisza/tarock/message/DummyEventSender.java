package com.tisza.tarock.message;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

import java.util.*;

public class DummyEventSender implements EventSender
{
	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcement)
	{

	}

	@Override
	public void announcePassz(PlayerSeat player)
	{

	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{

	}

	@Override
	public void call(PlayerSeat player, Card card)
	{

	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{

	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{

	}

	@Override
	public void throwCards(PlayerSeat player)
	{

	}

	@Override
	public void turn(PlayerSeat player)
	{

	}

	@Override
	public void startGame(PlayerSeat seat, List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
	{

	}

	@Override
	public void playerCards(PlayerCards cards)
	{

	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{

	}

	@Override
	public void availabeBids(Collection<Integer> bids)
	{

	}

	@Override
	public void availabeCalls(Collection<Card> cards)
	{

	}

	@Override
	public void changeDone(PlayerSeat player)
	{

	}

	@Override
	public void skartTarock(PlayerSeat.Map<Integer> counts)
	{

	}

	@Override
	public void availableAnnouncements(List<AnnouncementContra> announcements)
	{

	}

	@Override
	public void cardsTaken(PlayerSeat player)
	{

	}

	@Override
	public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points)
	{

	}

	@Override
	public void pendingNewGame()
	{

	}
}
