package com.tisza.tarock.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.EventProto.Event;
import com.tisza.tarock.proto.*;

import java.util.*;

public class Utils
{
	public static AnnouncementResult announcementResultFromProto(Event.Statistics.AnnouncementResult entry)
	{
		return new AnnouncementResult(announcementFromProto(entry.getAnnouncement()), entry.getPoints(), entry.getCallerTeam() ? Team.CALLER : Team.OPPONENT);
	}

	public static List<AnnouncementResult> staticticsListFromProto(List<Event.Statistics.AnnouncementResult> announcementResultProtoList)
	{
		List<AnnouncementResult> announcementResultList = new ArrayList<>();
		for (Event.Statistics.AnnouncementResult announcementResult : announcementResultProtoList)
		{
			announcementResultList.add(Utils.announcementResultFromProto(announcementResult));
		}
		return announcementResultList;
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
			builder.setCard(announcement.getCard().getID());
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
			announcement.setCard(Card.fromId(announcementProto.getCard()));
		if (announcementProto.hasRound())
			announcement.setRound(announcementProto.getRound());

		return announcement;
	}

	public static User userFromProto(MainProto.User userProto)
	{
		String imgURL = userProto.hasImageUrl() ? userProto.getImageUrl() : null;

		return new User(userProto.getId(), userProto.getName(), imgURL, userProto.getIsFriend(), userProto.getOnline());
	}

	public static GameInfo gameInfoFromProto(MainProto.Game gameProto)
	{
		return new GameInfo(gameProto.getId(), GameType.fromID(gameProto.getType()), gameProto.getPlayerNameList(), gameProto.getMy());
	}
}
