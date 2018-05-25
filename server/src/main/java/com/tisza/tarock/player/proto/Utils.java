package com.tisza.tarock.player.proto;

import com.tisza.tarock.announcement.Announcement;
import com.tisza.tarock.announcement.AnnouncementID;
import com.tisza.tarock.announcement.Announcements;
import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.PhaseEnum;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;

class Utils
{
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

	public static ProtoUtils.Phase phaseToProto(PhaseEnum pe)
	{
		switch (pe)
		{
			case BIDDING:
				return ProtoUtils.Phase.BIDDING;
			case CHANGING:
				return ProtoUtils.Phase.CHANGING;
			case CALLING:
				return ProtoUtils.Phase.CALLING;
			case ANNOUNCING:
				return ProtoUtils.Phase.ANNOUNCING;
			case GAMEPLAY:
				return ProtoUtils.Phase.GAMEPLAY;
			case FINISHED:
				return ProtoUtils.Phase.FINISHED;
			case END:
				return ProtoUtils.Phase.END;
			case INTERRUPTED:
				return ProtoUtils.Phase.INTERRUPTED;
		}
		return null;
	}

	public static ProtoUtils.Announcement announcementToProto(AnnouncementContra ac)
	{
		Announcement a = ac.getAnnouncement();
		ProtoUtils.Announcement.Builder builder = ProtoUtils.Announcement.newBuilder()
				.setName(a.getName())
				.setContraLevel(ac.getContraLevel());

		if (a.getCard() != null)
			builder.setCard(cardToProto(a.getCard()));
		if (a.getRound() >= 0)
			builder.setRound(a.getRound());
		if (a.getSuit() >= 0)
			builder.setSuit(a.getSuit());

		return builder.build();
	}

	public static EventProto.Event.AnnouncementStatistics.Entry statisticsEntryToProto(AnnouncementStaticticsEntry entry)
	{
		return EventProto.Event.AnnouncementStatistics.Entry.newBuilder()
				.setAnnouncement(announcementToProto(entry.getAnnouncementContra()))
				.setPoints(entry.getPoints())
				.build();
	}

	public static Card cardFromProto(ProtoUtils.Card cardProto)
	{
		switch (cardProto.getCardTypeCase())
		{
			case SUIT_CARD:
				ProtoUtils.SuitCard suitCardProto = cardProto.getSuitCard();
				return new SuitCard(suitCardProto.getSuit(), suitCardProto.getValue());
			case TAROCK_CARD:
				ProtoUtils.TarockCard tarockCardProto = cardProto.getTarockCard();
				return new TarockCard(tarockCardProto.getValue());
			default:
				throw new IllegalArgumentException();
		}
	}

	public static AnnouncementContra announcementFromProto(ProtoUtils.Announcement announcementProto)
	{
		AnnouncementID aid = new AnnouncementID(announcementProto.getName());
		if (announcementProto.hasSuit())
			aid.setSuit(announcementProto.getSuit());
		if (announcementProto.hasCard())
			aid.setCard(cardFromProto(announcementProto.getCard()));
		if (announcementProto.hasRound())
			aid.setRound(announcementProto.getRound());

		return new AnnouncementContra(Announcements.getByID(aid), announcementProto.getContraLevel());
	}
}
