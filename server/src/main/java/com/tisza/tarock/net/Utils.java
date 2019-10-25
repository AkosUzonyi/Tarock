package com.tisza.tarock.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.*;
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
