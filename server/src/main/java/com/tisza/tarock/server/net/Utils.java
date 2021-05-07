package com.tisza.tarock.server.net;

import com.tisza.tarock.game.*;
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

	public static MainProto.GameSession.State gameSessionStateToProto(GameSession.State state)
	{
		switch (state)
		{
			case LOBBY: return MainProto.GameSession.State.LOBBY;
			case GAME: return MainProto.GameSession.State.GAME;
			case DELETED: return MainProto.GameSession.State.DELETED;
			default: throw new IllegalArgumentException("unknown game state: " + state);
		}
	}
}
