package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

import java.util.*;

public class Announcements
{
	private static final List<Announcement> all = new ArrayList<Announcement>();
	
	public static final Jatek jatek = new Jatek();
	public static final HivatalbolKontraParti hkp = new HivatalbolKontraParti();
	public static final Trull trull = new Trull();
	public static final Banda[] bandak = new Banda[4];
	public static final Negykiraly negykiraly = new Negykiraly();
	public static final TarockCount nyolctarokk = new TarockCount(8);
	public static final TarockCount kilenctarokk = new TarockCount(9);
	public static final Dupla dupla = new Dupla();
	public static final HosszuDupla hosszuDupla = new HosszuDupla();
	public static final KezbeVacak centrum = new KezbeVacak(4, new TarockCard(20));
	public static final KezbeVacak kismadar = new KezbeVacak(5, new TarockCard(21));
	public static final KezbeVacak nagymadar = new KezbeVacak(6, new TarockCard(22));
	public static final Kings[] kings = new Kings[3];
	public static final Zaroparos zaroparos = new Zaroparos();
	public static final Szinesites szinesites = new Szinesites();
	public static final Volat volat = new Volat();
	public static final Kisszincsalad[] kisszincsaladok = new Kisszincsalad[4];
	public static final Nagyszincsalad[] nagyszincsaladok = new Nagyszincsalad[4];
	public static final Facan pagatfacan = new Facan(new TarockCard(1));
	public static final Facan sasfacan = new Facan(new TarockCard(2));
	public static final XXIFogas xxiFogas = new XXIFogas();
	public static final Map<Card, Map<Integer, Ultimo>> ultimok = new HashMap<Card, Map<Integer, Ultimo>>();
	
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
		all.add(jatek);
		all.add(hkp);
		all.add(trull);
		for (int s = 0; s < 4; s++)
		{
			bandak[s] = new Banda(s);
			all.add(bandak[s]);
		}
		all.add(negykiraly);
		all.add(nyolctarokk);
		all.add(kilenctarokk);
		all.add(dupla);
		all.add(hosszuDupla);
		all.add(centrum);
		all.add(kismadar);
		all.add(nagymadar);
		kings[0] = new Kings(1);
		all.add(kings[0]);
		all.add(zaroparos);
		all.add(szinesites);
		all.add(volat);
		kings[1] = new Kings(2);
		all.add(kings[1]);
		kings[2] = new Kings(3);
		all.add(kings[2]);
		
		for (int s = 0; s < 4; s++)
		{
			Kisszincsalad kisszincsalad = new Kisszincsalad(s);
			kisszincsaladok[s] = kisszincsalad;
			all.add(kisszincsalad);
		}
		
		for (int s = 0; s < 4; s++)
		{
			Nagyszincsalad nagyszincsalad = new Nagyszincsalad(s);
			nagyszincsaladok[s] = nagyszincsalad;
			all.add(nagyszincsalad);
		}
		
		all.add(pagatfacan);
		all.add(sasfacan);
		all.add(xxiFogas);
		
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				SuitCard card = new SuitCard(s, v);
				ultimok.put(card, new HashMap<Integer, Ultimo>());
				for (int roundIndex = 8; roundIndex >= 5; roundIndex--)
				{
					Ultimo announcement = new Szinultimo(roundIndex, card);
					ultimok.get(card).put(roundIndex, announcement);
					all.add(announcement);
				}
			}
		}
		
		for (int t = 1; t <= 2; t++)
		{
			TarockCard card = new TarockCard(t);
			ultimok.put(card, new HashMap<Integer, Ultimo>());
			for (int roundIndex = 8; roundIndex >= 5; roundIndex--)
			{
				Ultimo announcement = new PagatSasUltimo(roundIndex, card);
				ultimok.get(card).put(roundIndex, announcement);
				all.add(announcement);
			}
		}
		
		{
			Card card = new TarockCard(21);
			ultimok.put(card, new HashMap<Integer, Ultimo>());
			for (int roundIndex = 8; roundIndex >= 5; roundIndex--)
			{
				Ultimo announcement = new XXIUltimo(roundIndex);
				ultimok.get(card).put(roundIndex, announcement);
				all.add(announcement);
			}
		}
	}
}
