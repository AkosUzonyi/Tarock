package com.tisza.tarock.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.EventProto.Event;
import com.tisza.tarock.proto.*;

import java.util.*;
import java.util.function.*;

public class Utils
{
	public static AnnouncementResult announcementResultFromProto(Event.Statistics.AnnouncementResult entry)
	{
		return new AnnouncementResult(Announcement.fromID(entry.getAnnouncement()), entry.getPoints(), entry.getCallerTeam() ? Team.CALLER : Team.OPPONENT);
	}

	public static List<AnnouncementResult> staticticsListFromProto(List<Event.Statistics.AnnouncementResult> announcementResultProtoList)
	{
		return map(announcementResultProtoList, Utils::announcementResultFromProto);
	}

	public static List<Announcement> announcementListFromProto(List<String> announcementIDList)
	{
		return map(announcementIDList, Announcement::fromID);
	}

	public static User userFromProto(MainProto.User userProto)
	{
		String imgURL = userProto.hasImageUrl() ? userProto.getImageUrl() : null;

		return new User(userProto.getId(), userProto.getName(), imgURL, userProto.getIsFriend(), userProto.getOnline());
	}

	public static GameInfo gameInfoFromProto(MainProto.GameSession gameProto)
	{
		return new GameInfo(gameProto.getId(), GameType.fromID(gameProto.getType()), map(gameProto.getUserList(), Utils::userFromProto));
	}

	public static <T0, T1> List<T1> map(List<T0> list, Function<T0, T1> f)
	{
		List<T1> result = new ArrayList<>();
		for (T0 t : list)
			result.add(f.apply(t));
		return result;
	}
}
