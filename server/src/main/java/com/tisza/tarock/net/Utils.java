package com.tisza.tarock.net;

import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.*;

public class Utils
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
			case END:
				return ProtoUtils.Phase.END;
			case INTERRUPTED:
				return ProtoUtils.Phase.INTERRUPTED;
		}
		return null;
	}

	public static ProtoUtils.Announcement announcementToProto(AnnouncementContra ac)
	{
		AnnouncementID aid = ac.getAnnouncement().getID();
		ProtoUtils.Announcement.Builder builder = ProtoUtils.Announcement.newBuilder()
				.setName(aid.getName())
				.setContraLevel(ac.getContraLevel());

		if (aid.hasSuit())
			builder.setSuit(aid.getSuit());
		if (aid.hasCard())
			builder.setCard(cardToProto(aid.getCard()));
		if (aid.hasRound())
			builder.setRound(aid.getRound());

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
				return Card.getSuitCard(suitCardProto.getSuit(), suitCardProto.getValue());
			case TAROCK_CARD:
				ProtoUtils.TarockCard tarockCardProto = cardProto.getTarockCard();
				return Card.getTarockCard(tarockCardProto.getValue());
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

	public static GameType gameTypeFromProto(ProtoUtils.GameType gameTypeProto)
	{
		switch (gameTypeProto)
		{
			case PASKIEVICS:
				return GameType.PASKIEVICS;
			case ILLUSZTRALT:
				return GameType.ILLUSZTRALT;
			case MAGAS:
				return GameType.MAGAS;
			case ZEBI:
				return GameType.ZEBI;
		}
		throw new RuntimeException();
	}

	public static ProtoUtils.GameType gameTypeToProto(GameType gameTypeProto)
	{
		switch (gameTypeProto)
		{
			case PASKIEVICS:
				return ProtoUtils.GameType.PASKIEVICS;
			case ILLUSZTRALT:
				return ProtoUtils.GameType.ILLUSZTRALT;
			case MAGAS:
				return ProtoUtils.GameType.MAGAS;
			case ZEBI:
				return ProtoUtils.GameType.ZEBI;
		}
		throw new RuntimeException();
	}

	public static MainProto.User userToProto(User user, boolean isFriend)
	{
		MainProto.User.Builder builder = MainProto.User.newBuilder()
				.setId(user.getId())
				.setName(user.getName())
				.setIsFriend(isFriend)
				.setOnline(user.isLoggedIn());

		String imgURL = user.getImageURL();
		if (imgURL != null)
			builder.setImageUrl(imgURL);

		return builder.build();
	}

	public static MainProto.Game gameInfoToProto(GameInfo gameInfo)
	{
		return MainProto.Game.newBuilder()
				.setId(gameInfo.getId())
				.setType(gameTypeToProto(gameInfo.getType()))
				.addAllPlayerName(gameInfo.getPlayerNames())
				.build();
	}

	public static DoubleRoundType doubleRoundTypeFromProto(ProtoUtils.DoubleRoundType doubleRoundTypeProto)
	{
		switch (doubleRoundTypeProto)
		{
			case NONE:
				return DoubleRoundType.NONE;
			case PECULATING:
				return DoubleRoundType.PECULATING;
			case STACKING:
				return DoubleRoundType.STACKING;
			case MULTIPLYING:
				return DoubleRoundType.MULTIPLYING;
		}
		throw new RuntimeException();
	}
}
