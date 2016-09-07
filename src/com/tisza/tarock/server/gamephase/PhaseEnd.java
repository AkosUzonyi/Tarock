package com.tisza.tarock.server.gamephase;

import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.net.packet.PacketAnnouncementStatistics.Entry;
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
		
		Map<Team, List<PacketAnnouncementStatistics.Entry>> statEntriesForTeams = new HashMap<Team, List<PacketAnnouncementStatistics.Entry>>();
		statEntriesForTeams.put(Team.CALLER, new ArrayList<PacketAnnouncementStatistics.Entry>());
		statEntriesForTeams.put(Team.OPPONENT, new ArrayList<PacketAnnouncementStatistics.Entry>());
		
		Announcing announcing = game.getCurrentGame().announcing;
		
		for (Team team : Team.values())
		{
			for (Announcement announcement : Announcements.getAll())
			{
				boolean isAnnounced = announcing.isAnnounced(team, announcement);
				int contraMultiplier = (int)Math.pow(2, isAnnounced ? announcing.getContraLevel(team, announcement) : 0);
				int points = announcement.calculatePoints(game.getCurrentGame(), team, isAnnounced);
				points *= contraMultiplier;
				
				pointsForCallerTeam += points * (team == Team.CALLER ? 1 : -1);
				
				if (points != 0)
				{
					AnnouncementContra ac = new AnnouncementContra(announcement, isAnnounced ? announcing.getContraLevel(team, announcement) : -1);
					statEntriesForTeams.get(team).add(new PacketAnnouncementStatistics.Entry(ac, points));
				}
			}	
		}
		
		for (int p = 0; p < 4; p++)
		{
			Team team = pp.getTeam(p);
			List<Entry> selfEntries = statEntriesForTeams.get(team);
			List<Entry> opponentEntries = statEntriesForTeams.get(team.getOther());
			game.sendPacketToPlayer(p, new PacketAnnouncementStatistics(selfEntries, opponentEntries));
		}
		
		game.getPoints().addPoints(pointsForCallerTeam, pp.getCaller(), pp.getCalled());
		game.savePoints();
		game.broadcastPacket(new PacketPoints(game.getPoints().getCurrentPoints()));
		
		game.changeGamePhase(new PhasePendingNewGame(game, false));
	}

	public void packetFromPlayer(int player, Packet packet)
	{
	}

}
