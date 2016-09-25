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
	private Map<Team, Integer> gamePointsForTeams = new HashMap<Team, Integer>();
	private Map<Team, List<PacketAnnouncementStatistics.Entry>> statEntriesForTeams = new HashMap<Team, List<PacketAnnouncementStatistics.Entry>>();
	private int[] points = new int[4];
	private int pointsForCallerTeam;
	
	public PhaseEnd(GameSession g)
	{
		game = g;
		game.broadcastPacket(new PacketPhase(PacketPhase.Phase.END));
		
		Announcing announcing = game.getCurrentGame().announcing;
		pointsForCallerTeam = 0;
		
		for (Team team : Team.values())
		{
			gamePointsForTeams.put(team, GamePoints.calculateGamePoints(game.getCurrentGame(), team));
			
			ArrayList<Entry> entriesForTeam = new ArrayList<PacketAnnouncementStatistics.Entry>();
			statEntriesForTeams.put(team, entriesForTeam);
			
			for (Announcement announcement : Announcements.getAll())
			{
				int points = announcement.calculatePoints(game.getCurrentGame(), team);
				
				pointsForCallerTeam += points * (team == Team.CALLER ? 1 : -1);
				
				if (points != 0)
				{
					int acl = announcing.isAnnounced(team, announcement) ? announcing.getContraLevel(team, announcement) : -1;
					AnnouncementContra ac = new AnnouncementContra(announcement, acl);
					entriesForTeam.add(new PacketAnnouncementStatistics.Entry(ac, points));
				}
			}
		}
		
		for (int i = 0; i < 4; i++)
		{
			points[i] = -pointsForCallerTeam;
		}
		PlayerPairs pp = game.getCurrentGame().calling.getPlayerPairs();
		points[pp.getCaller()] += pointsForCallerTeam * 2;
		points[pp.getCalled()] += pointsForCallerTeam * 2;
	}
	
	public void start()
	{
		PlayerPairs pp = game.getCurrentGame().calling.getPlayerPairs();
		
		game.addPoints(points);
		
		for (int i = 0; i < 4; i++)
		{
			Team team = pp.getTeam(i);
			int selfGamePoints = gamePointsForTeams.get(team);
			int opponentGamePoints = gamePointsForTeams.get(team.getOther());
			List<Entry> selfEntries = statEntriesForTeams.get(team);
			List<Entry> opponentEntries = statEntriesForTeams.get(team.getOther());
			int sumPoints = pointsForCallerTeam * (team == Team.CALLER ? 1 : -1);
			game.sendPacketToPlayer(i, new PacketAnnouncementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, game.getPoints()));
		}
		
		game.changeGamePhase(new PhasePendingNewGame(game, false));
	}

	public void playerLoggedIn(int player)
	{
		game.sendPacketToPlayer(player, new PacketPhase(PacketPhase.Phase.END));		
	}

	public void packetFromPlayer(int player, Packet packet)
	{
	}

}
