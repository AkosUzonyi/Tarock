package com.tisza.tarock.server.gamephase;

import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseEnd implements GamePhase
{
	private GameSession game;
	
	public PhaseEnd(GameSession g)
	{
		game = g;
	}
	
	public void start()
	{
		int pointsForCallerTeam = 0;
		
		PlayerPairs pp = game.getCurrentGame().calling.getPlayerPairs();
		
		//TODO: implement different statistics for teams
		List<PacketAnnouncementStatistics.Entry> statEntriesForCallerTeam = new ArrayList<PacketAnnouncementStatistics.Entry>();
		
		for (Map.Entry<Announcement, AnnouncementState> announcementEntry : game.getCurrentGame().announcing.getAnnouncementStates().entrySet())
		{
			Announcement a = announcementEntry.getKey();
			AnnouncementState as = announcementEntry.getValue();
			
			for (Team t : Team.values())
			{
				AnnouncementState.PerTeam aspt = as.team(t);
				int contraLevel = aspt.isAnnounced() ? aspt.getContraLevel() : 0;
				int points = a.calculatePoints(game.getCurrentGame(), t, aspt.isAnnounced()) * (int)Math.pow(2, contraLevel);
				pointsForCallerTeam += points * (t == Team.CALLER ? 1 : -1);
				
				if (points != 0 && a instanceof AnnouncementBase)
				{
					AnnouncementBase ab = (AnnouncementBase)a;
					AnnouncementBase.Result result = ab.isSuccessful(game.getCurrentGame(), t);
					statEntriesForCallerTeam.add(new PacketAnnouncementStatistics.Entry(a, contraLevel, result, pointsForCallerTeam));
				}
			}
		}
		
		game.broadcastPacket(new PacketAnnouncementStatistics(statEntriesForCallerTeam));
		
		game.getPoints().addPoints(pointsForCallerTeam, pp.getCaller(), pp.getCalled());
		game.savePoints();
		game.broadcastPacket(new PacketPoints(game.getPoints().getCurrentPoints()));
		game.changeGamePhase(new PhasePendingNewGame(game, false));
	}

	public void packetFromPlayer(int player, Packet packet)
	{
	}

}
