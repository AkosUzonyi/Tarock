package com.tisza.tarock.server;

import java.io.*;
import java.util.*;

public class Points
{
	private List<String> playerNames = new ArrayList<String>();
	private List<String> sortedPlayerNames = new ArrayList<String>();
	
	private LinkedList<Entry> pointEntries = new LinkedList<Entry>();
	private int dirtyEntries = 0;
	
	public Points(List<String> names)
	{
		if (names.size() != 4)
			throw new IllegalArgumentException();
		
		playerNames = new ArrayList<String>(names);
		sortedPlayerNames = new ArrayList<String>(playerNames);
		Collections.sort(sortedPlayerNames);
		
		pointEntries.addLast(Entry.ITINITAL_ENTRY);
		dirtyEntries++;
	}

	public void addPoints(int pointsForCallerTeam, int caller, int called)
	{
		int c0 = getColumnIndexForPlayer(caller);
		int c1 = getColumnIndexForPlayer(called);
		Entry lastEntry = pointEntries.getLast();
		
		int[] newPoints = new int[4];
		for (int i = 0; i < 4; i++)
		{
			newPoints[i] = lastEntry.getPoint(i) - pointsForCallerTeam;
		}
		
		newPoints[c0] += pointsForCallerTeam * 2;
		newPoints[c1] += pointsForCallerTeam * 2;
		
		pointEntries.addLast(new Entry(newPoints));
		dirtyEntries++;
	}
	
	private int getColumnIndexForPlayer(int playerID)
	{
		return sortedPlayerNames.indexOf(playerNames.get(playerID));
	}
	
	public Entry getCurrentPoints()
	{
		return pointEntries.getLast();
	}
	
	public void readData(InputStream is) throws IOException
	{
		pointEntries.clear();
		DataInputStream dis = new DataInputStream(is);
		
		while (is.available() > 0)
		{
			int[] points = new int[4];
			for (int j = 0; j < 4; j++)
			{
				points[j] = dis.readInt();
			}
			pointEntries.addLast(new Entry(points));
		}
		
		dirtyEntries = 0;
		
		if (pointEntries.size() == 0)
		{
			pointEntries.addLast(Entry.ITINITAL_ENTRY);
		}
	}
	
	public void writeData(OutputStream os) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(os);
		
		for (Entry e : pointEntries)
		{
			for (int i = 0; i < 4; i++)
			{
				dos.writeInt(e.getPoint(i));
			}
		}
		dirtyEntries = 0;
	}
	
	public void appendDirtyData(OutputStream os) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(os);
		
		ListIterator<Entry> iterator = pointEntries.listIterator(pointEntries.size());
		
		for (; dirtyEntries > 0; dirtyEntries--)
		{
			iterator.previous();
		}
		
		while (iterator.hasNext())
		{
			Entry entry = iterator.next();
			for (int i = 0; i < 4; i++)
			{
				dos.writeInt(entry.getPoint(i));
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
	}
}
