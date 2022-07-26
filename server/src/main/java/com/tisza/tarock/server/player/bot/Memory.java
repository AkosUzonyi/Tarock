package com.tisza.tarock.server.player.bot;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import lombok.*;

import java.util.*;

@Data public class Memory
{
	private PlayerCards myCards;
	private GameType gameType;
	private Card currentFirstCard = null;
	private Card currentStrongestCard = null;
	private int cardsInTrick = 0;
	private int round = 1;
	private int beginnerPlayer;

	private PlayerSeatMap<Set<AnnouncementContra>> announcements;
	private PlayerSeatMap<Integer> bids;
	private PlayerSeat caller;
	private Card calledCard;
	private int usedTarocksNumber = 0;
	private PlayerSeatMap<Set<Card>> playedCards;
	private PlayerSeatMap<Integer> foldedTarocks;

	public void updateCardsInTrick()
	{
		cardsInTrick++;

		if (cardsInTrick == 4)
		{
			currentFirstCard = null;
			currentStrongestCard = null;
			round++;
		}

		cardsInTrick %= 4;
	}

	public void addAnnouncement(PlayerSeat player, AnnouncementContra announcement)
	{
		announcements.putIfAbsent(player, new HashSet<>());
		announcements.get(player).add(announcement);
		//TODO normalize announcements
	}

	public void addPlayedCard(PlayerSeat player, Card card)
	{
		playedCards.putIfAbsent(player, new HashSet<>());
		playedCards.get(player).add(card);

		if (card instanceof TarockCard)
		{
			usedTarocksNumber++;
		}
	}
}
