package com.tisza.tarock.server;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.server.gamephase.*;

public class Points
{
	private Deque<Entry> pointEntries = new LinkedList<Entry>();
	
	public Points()
	{
		pointEntries.addLast(Entry.ITINITAL_ENTRY);
	}
	
	public void evaluateGame(GameHistory gh)
	{
		int pointsForCallerTeam = 0;
		
		Map<Announcement, AnnouncementState> announcementStates = gh.announcing.getAnnouncementStates();
		for (Map.Entry<Announcement, AnnouncementState> announcementEntry : announcementStates.entrySet())
		{
			Announcement a = announcementEntry.getKey();
			AnnouncementState as = announcementEntry.getValue();
			
			Gameplay gp = gh.gameplay;
			PlayerPairs pp = gh.calling.getPlayerPairs();
			int winnerBid = gh.bidding.getWinnerBid();
			pointsForCallerTeam += a.calculatePoints(gp, pp, Team.CALLER, winnerBid, as.team(Team.CALLER).isAnnounced());
		}
	}
	
	public void readData(InputStream is) throws IOException
	{
		pointEntries.clear();
		DataInputStream dis = new DataInputStream(is);
		int size = dis.readInt();
		for (int i = 0; i < size; i++)
		{
			int[] points = new int[4];
			for (int j = 0; j < 4; j++)
			{
				points[j] = dis.readInt();
			}
			pointEntries.addLast(new Entry(points));
		}
	}
	
	public void writeData(OutputStream os) throws IOException
	{
		pointEntries.clear();
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(pointEntries.size());
		for (Entry e : pointEntries)
		{
			for (int i = 0; i < 4; i++)
			{
				dos.writeInt(e.getPoint(i));
			}
		}
	}
	
	public static class Entry
	{
		public static final Entry ITINITAL_ENTRY = new Entry(0, 0, 0, 0);
		
		private int[] data = new int[4];
		
		public Entry(int ... d)
		{
			if (d.length != 4) throw new IllegalArgumentException();
			System.arraycopy(d, 0, data, 0, 4);
		}
		
		public int getPoint(int player)
		{
			return data[player];
		}
		
		public Entry addPoints(int amount, int caller, int called)
		{
			int[] newData = data.clone();
			for (int i = 0; i < 4; i++)
			{
				newData[i] -= amount;
			}
			newData[caller] += amount * 2;
			newData[called] += amount * 2;
			return new Entry(newData);
		}
	}
}
