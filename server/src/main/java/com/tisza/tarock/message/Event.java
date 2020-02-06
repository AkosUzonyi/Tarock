package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.server.database.*;

import java.util.*;

public interface Event
{
	void handle(EventHandler handler);

	static Event announce(PlayerSeat player, AnnouncementContra announcement)
	{
		return handler -> handler.announce(player, announcement);
	}

	static Event announcePassz(PlayerSeat player)
	{
		return handler -> handler.announcePassz(player);
	}

	static Event bid(PlayerSeat player, int bid)
	{
		return handler -> handler.bid(player, bid);
	}

	static Event call(PlayerSeat player, Card card)
	{
		return (handler -> handler.call(player, card));
	}

	static Event playCard(PlayerSeat player, Card card)
	{
		return handler -> handler.playCard(player, card);
	}

	static Event readyForNewGame(PlayerSeat player)
	{
		return handler -> handler.readyForNewGame(player);
	}

	static Event throwCards(PlayerSeat player)
	{
		return handler -> handler.throwCards(player);
	}

	static Event turn(PlayerSeat player)
	{
		return handler -> handler.turn(player);
	}

	static Event playerTeamInfo(PlayerSeat otherPlayer, Team team)
	{
		return handler -> handler.playerTeamInfo(otherPlayer, team);
	}

	static Event startGame(GameType gameType, PlayerSeat beginnerPlayer)
	{
		return handler -> handler.startGame(gameType, beginnerPlayer);
	}

	static Event gameSessionState(GameSession.State state)
	{
		return handler -> handler.gameSessionState(state);
	}

	static Event player(PlayerSeat seat, User user)
	{
		return handler -> handler.player(seat, user);
	}

	static Event playerCards(PlayerCards cards)
	{
		return handler -> handler.playerCards(cards);
	}

	static Event phaseChanged(PhaseEnum phase)
	{
		return handler -> handler.phaseChanged(phase);
	}

	static Event availableBids(Collection<Integer> bids)
	{
		return handler -> handler.availableBids(bids);
	}

	static Event availableCalls(Collection<Card> cards)
	{
		return handler -> handler.availableCalls(cards);
	}

	static Event changeDone(PlayerSeat player)
	{
		return handler -> handler.changeDone(player);
	}

	static Event skartTarock(PlayerSeatMap<Integer> counts)
	{
		return handler -> handler.skartTarock(counts);
	}

	static Event availableAnnouncements(List<AnnouncementContra> announcements)
	{
		return handler -> handler.availableAnnouncements(announcements);
	}

	static Event cardsTaken(PlayerSeat player)
	{
		return handler -> handler.cardsTaken(player);
	}

	static Event announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier)
	{
		return handler -> handler.announcementStatistics(selfGamePoints, opponentGamePoints, announcementResults, sumPoints, pointMultiplier);
	}

	static Event playerPoints(int[] points)
	{
		return handler -> handler.playerPoints(points);
	}

	static Event pendingNewGame()
	{
		return handler -> handler.pendingNewGame();
	}

	static Event historyMode(boolean isHistory)
	{
		return handler -> handler.historyMode(isHistory);
	}

	static Event chat(PlayerSeat player, String message)
	{
		return handler -> handler.chat(player, message);
	}
}
