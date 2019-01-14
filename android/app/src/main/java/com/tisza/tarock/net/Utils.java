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
		return new AnnouncementResult(Announcement.fromID(entry.getAnnouncement()), entry.getPoints(), entry.getCallerTeam() ? Team.CALLER : Team.OPPONENT);
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

	public static List<Announcement> announcementListFromProto(List<String> announcementIDList)
	{
		List<Announcement> announcements = new ArrayList<>();
		for (String announcementID : announcementIDList)
		{
			announcements.add(Announcement.fromID(announcementID));
		}
		return announcements;
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
