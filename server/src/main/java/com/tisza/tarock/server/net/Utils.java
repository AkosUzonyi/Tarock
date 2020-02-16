package com.tisza.tarock.server.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.server.database.*;
import io.reactivex.*;

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
}
