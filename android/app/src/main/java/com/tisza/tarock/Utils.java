package com.tisza.tarock;

import com.tisza.tarock.game.*;
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
		return map(announcementResultProtoList, Utils::announcementResultFromProto);
	}

	public static List<Announcement> announcementListFromProto(List<String> announcementIDList)
	{
		return map(announcementIDList, Announcement::fromID);
	}

	public static User userFromProto(MainProto.User userProto)
	{
		String imgURL = userProto.hasImageUrl() ? userProto.getImageUrl() : null;

		return new User(userProto.getId(), userProto.getName(), imgURL, userProto.getIsFriend(), userProto.getOnline(), userProto.getBot());
	}

	public static <T0, T1> List<T1> map(List<T0> list, Function<T0, T1> f)
	{
		List<T1> result = new ArrayList<>();
		for (T0 t : list)
			result.add(f.apply(t));
		return result;
	}

	public static boolean equals(Object o0, Object o1)
	{
		if (o0 == null)
			return o1 == null;

		return o0.equals(o1);
	}

	public static GameSessionState gameSessionStateFromProto(MainProto.GameSession.State state)
	{
		switch (state)
		{
			case LOBBY: return GameSessionState.LOBBY;
			case GAME: return GameSessionState.GAME;
			case ENDED: return GameSessionState.ENDED;
			default: throw new IllegalArgumentException("unknown game state: " + state);
		}
	}

	public interface Function<T0, T1>
	{
		T1 apply(T0 param);
	}
}
