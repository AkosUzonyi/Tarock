package com.tisza.tarock.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.*;

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
