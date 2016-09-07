package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Announcements
{
	private static final List<Announcement> all = new ArrayList<Announcement>();
	
	public static final Jatek game = new Jatek();
	public static final Trull trull = new Trull();
	public static final XXIFogas xxiFogas = new XXIFogas();
	public static final Banda[] bandak = new Banda[4];
	public static Negykiraly negykiraly = new Negykiraly();
	public static Dupla dupla = new Dupla();
	public static HosszuDupla hosszuDupla = new HosszuDupla();
	public static Centrum centrum = new Centrum();
	public static Kismadar kismadar = new Kismadar();
	public static Nagymadar nagymadar = new Nagymadar();
	public static Map<Integer, Map<Card, TakeRoundWithCard>> ultimok = new HashMap<Integer, Map<Card, TakeRoundWithCard>>();
	
	public static Collection<Announcement> getAll()
	{
		return all;
	}
	
	public static Announcement getFromID(int id)
	{
		return all.get(id);
	}
	
	public static int getID(Announcement a)
	{
		return all.indexOf(a);
	}
	
	static
	{
		all.add(game);
		all.add(trull);
		all.add(xxiFogas);
		for (int s = 0; s < 4; s++)
		{
			bandak[s] = new Banda(s);
			all.add(bandak[s]);
		}
		all.add(negykiraly);
		all.add(dupla);
		all.add(hosszuDupla);
		all.add(centrum);
		all.add(kismadar);
		all.add(nagymadar);
		
		for (int j = 0; j < 4; j++)
		{
			int roundIndex = 8 - j;
			
			Map<Card, TakeRoundWithCard> map = new HashMap<Card, TakeRoundWithCard>();
			
			for (int t = 1; t <= 2; t++)
			{
				TarockCard c = new TarockCard(t);
				TakeRoundWithCard announcement = new TakeRoundWithTarock(roundIndex, c);
				map.put(c, announcement);
				all.add(announcement);
			}
			
			{
				TakeRoundWithCard announcement = new XXIUltimo(roundIndex);
				map.put(new TarockCard(21), announcement);
				all.add(announcement);
			}
			
			for (int s = 0; s < 4; s++)
			{
				for (int v = 1; v <= 5; v++)
				{
					SuitCard c = new SuitCard(s, v);
					TakeRoundWithCard announcement = new TakeRoundWithSuitCard(roundIndex, c);
					map.put(c, announcement);
					all.add(announcement);
				}
			}
			
			ultimok.put(roundIndex, map);
		}
		
		
	}
}
