package com.tisza.tarock.server.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.server.database.*;
import io.reactivex.*;

import java.util.*;

public class Utils
{
	public static EventProto.Event.Statistics.AnnouncementResult announcementResultToProto(AnnouncementResult entry)
	{
		return EventProto.Event.Statistics.AnnouncementResult.newBuilder()
				.setAnnouncement(entry.getAnnouncementContra().getID())
				.setPoints(entry.getPoints())
				.setCallerTeam(entry.getTeam() == Team.CALLER)
				.build();
	}

	public static Single<MainProto.User> userToProto(User user, boolean isFriend, boolean loggedIn)
	{
		return Single.zip(user.getName(), user.getImageURL(), (name, imgURL) ->
		{
			MainProto.User.Builder builder = MainProto.User.newBuilder()
					.setId(user.getID())
					.setName(name)
					.setIsFriend(isFriend)
					.setOnline(loggedIn);

			imgURL.ifPresent(builder::setImageUrl);

			return builder.build();
		});
	}

	public static MainProto.GameSession.State gameSessionStateToProto(GameSession.State state)
	{
		switch (state)
		{
			case LOBBY: return MainProto.GameSession.State.LOBBY;
			case GAME: return MainProto.GameSession.State.GAME;
			case ENDED: return MainProto.GameSession.State.ENDED;
			default: throw new IllegalArgumentException("unknown game state: " + state);
		}
	}

	public static List<AnnouncementContra> announcementListFromProto(List<String> announcementIDList)
	{
		return map(announcementIDList, AnnouncementContra::fromID);
	}

	public static <T0, T1> List<T1> map(List<T0> list, Function<T0, T1> f)
	{
		List<T1> result = new ArrayList<>();
		for (T0 t : list)
			result.add(f.apply(t));
		return result;
	}

	public interface Function<T0, T1>
	{
		T1 apply(T0 param);
	}
}
