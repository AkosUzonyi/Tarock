package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import org.json.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class GameHistory
{
	private PlayerSeat.Map<List<Card>> originalPlayersCards = new PlayerSeat.Map<>();
	private List<BidEntry> bids = new ArrayList<>();
	private PlayerSeat.Map<List<Card>> cardsFromTalon = new PlayerSeat.Map<>();
	private PlayerSeat.Map<List<Card>> skartedCards = new PlayerSeat.Map<>();
	private PlayerSeat callerPlayer;
	private Card calledCard;
	private List<AnnouncementEntry> announcements = new ArrayList<>();
	private List<Round> rounds = new ArrayList<>();

	public void setOriginalPlayersCards(PlayerSeat player, List<Card> cards)
	{
		this.originalPlayersCards.put(player, cards);
	}

	public void registerBid(PlayerSeat player, int bid)
	{
		bids.add(new BidEntry(player, bid));
	}

	public void setCardsFromTalon(PlayerSeat player, List<Card> cards)
	{
		cardsFromTalon.put(player, cards);
	}

	public void setCardsSkarted(PlayerSeat player, List<Card> cards)
	{
		skartedCards.put(player, cards);
	}

	public void setCalledCard(PlayerSeat player, Card card)
	{
		callerPlayer = player;
		calledCard = card;
	}

	public void registerAnnouncement(PlayerSeat player, AnnouncementContra announcement)
	{
		announcements.add(new AnnouncementEntry(player, announcement));
	}

	public void registerRound(Round round)
	{
		rounds.add(round);
	}

	public void sendCurrentStatusToPlayer(PlayerSeat player, PhaseEnum currentPhase, EventSender eventSender)
	{
		for (BidEntry bidEntry : bids)
		{
			eventSender.bid(bidEntry.getPlayer(), bidEntry.getBid());
		}

		if (!currentPhase.isAfter(PhaseEnum.CALLING))
			return;

		eventSender.call(callerPlayer, calledCard);

		for (AnnouncementEntry announcementEntry : announcements)
		{
			eventSender.announce(announcementEntry.getPlayer(), announcementEntry.getAnnouncement());
		}

		if (!currentPhase.isAfter(PhaseEnum.ANNOUNCING))
			return;

		for (Round round : rounds)
		{
			PlayerSeat cardPlayer = round.getBeginnerPlayer();
			for (int i = 0; i < 4; i++)
			{
				eventSender.playCard(cardPlayer, round.getCardByPlayer(cardPlayer));
				cardPlayer = cardPlayer.nextPlayer();
			}
			eventSender.cardsTaken(round.getWinner());
		}
	}

	public void writeJSON(Writer writer) throws JSONException, IOException
	{
		JSONObject obj = new JSONObject();
		obj.put("cards", playermaplistcardsToJSON(originalPlayersCards));
		obj.put("bids", new JSONArray(bids.stream().map(BidEntry::toJSON).collect(Collectors.toList())));
		obj.put("talon", playermaplistcardsToJSON(cardsFromTalon));
		obj.put("skart", playermaplistcardsToJSON(skartedCards));
		obj.put("called", cardToJSON(calledCard));
		obj.put("announcements", announcements.stream().map(AnnouncementEntry::toJSON).collect(Collectors.toList()));
		obj.put("rounds", rounds.stream().map(GameHistory::roundToJSON).collect(Collectors.toList()));
		System.out.println(obj.toString());
		obj.write(writer);
		writer.flush();
	}

	private static Object roundToJSON(Round round)
	{
		JSONObject result = new JSONObject();
		result.put("beginner", round.getBeginnerPlayer().asInt());
		for (int i = 0; i < 4; i++)
		{
			result.append("cards", cardToJSON(round.getCardByIndex(i)));
		}
		return result;
	}

	private static Object playermaplistcardsToJSON(PlayerSeat.Map<List<Card>> cards)
	{
		return new JSONArray(cards.values().stream().map(list -> list.stream().map(GameHistory::cardToJSON).collect(Collectors.toList())).collect(Collectors.toList()));
	}

	private static Object cardToJSON(Card card)
	{
		JSONObject result = new JSONObject();
		if (card instanceof SuitCard)
		{
			SuitCard suitCard = (SuitCard)card;
			result.put("type", "suit");
			result.put("suit", suitCard.getSuit());
			result.put("value", suitCard.getValue());
		}
		else if (card instanceof TarockCard)
		{
			TarockCard suitCard = (TarockCard)card;
			result.put("type", "tarock");
			result.put("value", suitCard.getValue());
		}
		else
		{
			throw new RuntimeException();
		}
		return result;
	}

	private static class BidEntry
	{
		private final PlayerSeat player;
		private final int bid;

		public BidEntry(PlayerSeat player, int bid)
		{
			this.player = player;
			this.bid = bid;
		}

		public PlayerSeat getPlayer()
		{
			return player;
		}

		public int getBid()
		{
			return bid;
		}

		public Object toJSON() throws JSONException
		{
			JSONObject obj = new JSONObject();
			obj.put("player", player.asInt());
			obj.put("bid", bid);
			return obj;
		}
	}

	private static class AnnouncementEntry
	{
		private final PlayerSeat player;
		private final AnnouncementContra announcement;

		public AnnouncementEntry(PlayerSeat player, AnnouncementContra announcement)
		{
			this.player = player;
			this.announcement = announcement;
		}

		public PlayerSeat getPlayer()
		{
			return player;
		}

		public AnnouncementContra getAnnouncement()
		{
			return announcement;
		}

		public Object toJSON() throws JSONException
		{
			JSONObject obj = new JSONObject();
			obj.put("player", player);

			AnnouncementID aid = announcement.getAnnouncement().getID();
			obj.put("announcement", aid.getName());
			obj.put("contraLevel", announcement.getContraLevel());
			if (aid.hasSuit())
				obj.put("suit", aid.getSuit());
			if (aid.hasCard())
				obj.put("card", cardToJSON(aid.getCard()));
			if (aid.hasRound())
				obj.put("round", aid.getRound());

			return obj;
		}
	}
}
