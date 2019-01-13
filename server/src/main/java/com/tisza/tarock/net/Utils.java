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

	public static EventProto.Event.Statistics.AnnouncementResult announcementResultToProto(AnnouncementResult entry)
	{
		return EventProto.Event.Statistics.AnnouncementResult.newBuilder()
				.setAnnouncement(announcementToProto(entry.getAnnouncementContra()))
				.setPoints(entry.getPoints())
				.setCallerTeam(entry.getTeam() == Team.CALLER)
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

	public static MainProto.Game gameInfoToProto(GameInfo gameInfo, boolean my)
	{
		return MainProto.Game.newBuilder()
				.setId(gameInfo.getId())
				.setType(gameInfo.getType().getID())
				.addAllPlayerName(gameInfo.getPlayerNames())
				.setMy(my)
				.build();
	}
}
