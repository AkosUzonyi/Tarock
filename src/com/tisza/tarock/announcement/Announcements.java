package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Announcements
{
	private static final Map<Integer, Announcement> idToAnnouncement = new HashMap<Integer, Announcement>();
	private static final Map<Announcement, Integer> announcementToID = new HashMap<Announcement, Integer>();
	
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
	public static Map<Card, Map<Integer, TakeRoundWithCard>> ultimok = new HashMap<Card, Map<Integer, TakeRoundWithCard>>();
	
	public static Collection<Announcement> getAll()
	{
		return idToAnnouncement.values();
	}
	
	public static Announcement getFromID(int id)
	{
		return idToAnnouncement.get(id);
	}
	
	public static int getID(Announcement a)
	{
		return announcementToID.get(a);
	}
	
	private static void register(int id, Announcement a)
	{
		if (idToAnnouncement.containsKey(id))
		{
			System.err.println("Duplicate announcement id");
		}
		else
		{
			idToAnnouncement.put(id, a);
			announcementToID.put(a, id);
		}
	}
	
	static
	{
		register(0, game);
		register(1, trull);
		register(2, xxiFogas);
		for (int s = 0; s < 4; s++)
		{
			bandak[s] = new Banda(s);
			register(3 + s, bandak[s]);
		}
		register(7, negykiraly);
		register(8, dupla);
		register(9, hosszuDupla);
		register(10, centrum);
		register(11, kismadar);
		register(12, nagymadar);
		
		List<Card> ultimoCards = new ArrayList<Card>();
		ultimoCards.add(new TarockCard(1));
		ultimoCards.add(new TarockCard(2));
		ultimoCards.add(new TarockCard(21));
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				ultimoCards.add(new SuitCard(s, v));
			}
		}
		for (int i = 0; i < ultimoCards.size(); i++)
		{
			Card card = ultimoCards.get(i);
			Map<Integer, TakeRoundWithCard> map = new HashMap<Integer, TakeRoundWithCard>();
			for (int j = 0; j < 4; j++)
			{
				int roundIndex = 8 - j;
				TakeRoundWithCard ann = new TakeRoundWithCard(roundIndex, card);
				map.put(roundIndex, ann);
				register(13 + i * 4 + j, ann);
			}
			ultimok.put(card, map);
		}
		
		
	}
}
