package com.tisza.tarock.net;

import com.tisza.tarock.Announcement;
import com.tisza.tarock.PhaseEnum;
import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.EventProto.Event;
import com.tisza.tarock.proto.ProtoUtils;

import java.util.ArrayList;
import java.util.List;

class Utils
{
	public static AnnouncementStaticticsEntry staticticsFromProto(Event.AnnouncementStatistics.Entry entry)
	{
		return new AnnouncementStaticticsEntry(announcementFromProto(entry.getAnnouncement()), entry.getPoints());
	}

	public static List<AnnouncementStaticticsEntry> staticticsListFromProto(List<Event.AnnouncementStatistics.Entry> statisticsProtoList)
	{
		List<AnnouncementStaticticsEntry> announcements = new ArrayList<>();
		for (Event.AnnouncementStatistics.Entry statistics : statisticsProtoList)
		{
			announcements.add(Utils.staticticsFromProto(statistics));
		}
		return announcements;
	}

	public static List<Announcement> announcementListFromProto(List<ProtoUtils.Announcement> announcementProtoList)
	{
		List<Announcement> announcements = new ArrayList<>();
		for (ProtoUtils.Announcement announcement : announcementProtoList)
		{
			announcements.add(Utils.announcementFromProto(announcement));
		}
		return announcements;
	}

	public static ProtoUtils.Announcement announcementToProto(Announcement announcement)
	{
		ProtoUtils.Announcement.Builder builder = ProtoUtils.Announcement.newBuilder()
				.setName(announcement.getName())
				.setContraLevel(announcement.getContraLevel());

		if (announcement.hasCard())
			builder.setCard(cardToProto(announcement.getCard()));
		if (announcement.hasRound())
			builder.setRound(announcement.getRound());
		if (announcement.hasSuit())
			builder.setSuit(announcement.getSuit());

		return builder.build();
	}

	public static List<ProtoUtils.Announcement> announcementListToProto(List<Announcement> announcementList)
	{
		List<ProtoUtils.Announcement> announcements = new ArrayList<>();
		for (Announcement announcement : announcementList)
		{
			announcements.add(Utils.announcementToProto(announcement));
		}
		return announcements;
	}

	public static Announcement announcementFromProto(ProtoUtils.Announcement announcementProto)
	{
		Announcement announcement = new Announcement(announcementProto.getName(), announcementProto.getContraLevel());

		if (announcementProto.hasSuit())
			announcement.setSuit(announcementProto.getSuit());
		if (announcementProto.hasCard())
			announcement.setCard(cardFromProto(announcementProto.getCard()));
		if (announcementProto.hasRound())
			announcement.setRound(announcementProto.getRound());

		return announcement;
	}

	public static List<Card> cardListFromProto(List<ProtoUtils.Card> cardProtoList)
	{
		List<Card> cards = new ArrayList<>();
		for (ProtoUtils.Card card : cardProtoList)
		{
			cards.add(Utils.cardFromProto(card));
		}
		return cards;
	}

	public static Card cardFromProto(ProtoUtils.Card cardProto)
	{
		switch (cardProto.getCardTypeCase())
		{
			case SUIT_CARD:
				ProtoUtils.SuitCard suitCardProto = cardProto.getSuitCard();
				return Card.getSuitCard(suitCardProto.getSuit(), suitCardProto.getValue());
			case TAROCK_CARD:
				ProtoUtils.TarockCard tarockCardProto = cardProto.getTarockCard();
				return Card.getTarockCard(tarockCardProto.getValue());
			default:
				throw new IllegalArgumentException();
		}
	}

	public static PhaseEnum phaseFromProto(ProtoUtils.Phase pe)
	{
		switch (pe)
		{
			case BIDDING:
				return PhaseEnum.BIDDING;
			case CHANGING:
				return PhaseEnum.CHANGING;
			case CALLING:
				return PhaseEnum.CALLING;
			case ANNOUNCING:
				return PhaseEnum.ANNOUNCING;
			case GAMEPLAY:
				return PhaseEnum.GAMEPLAY;
			case FINISHED:
				return PhaseEnum.FINISHED;
			case END:
				return PhaseEnum.END;
			case INTERRUPTED:
				return PhaseEnum.INTERRUPTED;
		}
		return null;
	}

	public static List<ProtoUtils.Card> cardListToProto(List<Card> cardList)
	{
		List<ProtoUtils.Card> cards = new ArrayList<>();
		for (Card card : cardList)
		{
			cards.add(Utils.cardToProto(card));
		}
		return cards;
	}

	public static ProtoUtils.Card cardToProto(Card c)
	{
		ProtoUtils.Card.Builder protocard = ProtoUtils.Card.newBuilder();

		if (c instanceof SuitCard)
		{
			protocard.setSuitCard(ProtoUtils.SuitCard.newBuilder()
					.setSuit(((SuitCard)c).getSuit())
					.setValue(((SuitCard)c).getValue()));
		}
		else if (c instanceof TarockCard)
		{
			protocard.setTarockCard(ProtoUtils.TarockCard.newBuilder()
					.setValue(((TarockCard)c).getValue()));
		}
		else
		{
			throw new Error();
		}

		return protocard.build();
	}
}
