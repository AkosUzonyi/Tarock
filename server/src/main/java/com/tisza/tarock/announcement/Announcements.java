package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.card.TarockCard;

import java.util.*;

public class Announcements
{
	private static final Map<AnnouncementID, Announcement> idmap = new HashMap<>();
	private static final List<Announcement> list = new ArrayList<>();

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
		return list;
	}
	
	public static Announcement getByID(AnnouncementID id)
	{
		return idmap.get(id);
	}

	public static int getPosition(Announcement a)
	{
		return list.indexOf(a);
	}

	private static void add(Announcement a)
	{
		idmap.put(a.getID(), a);
		list.add(a);
	}

	static
	{
		add(jatek);
		add(hkp);
		add(trull);
		for (int s = 0; s < 4; s++)
		{
			bandak[s] = new Banda(s);
			add(bandak[s]);
		}
		add(negykiraly);
		add(nyolctarokk);
		add(kilenctarokk);
		add(dupla);
		add(hosszuDupla);
		add(centrum);
		add(kismadar);
		add(nagymadar);
		kings[0] = new Kings(1);
		add(kings[0]);
		add(zaroparos);
		add(szinesites);
		add(volat);
		kings[1] = new Kings(2);
		add(kings[1]);
		kings[2] = new Kings(3);
		add(kings[2]);
		
		for (int s = 0; s < 4; s++)
		{
			Kisszincsalad kisszincsalad = new Kisszincsalad(s);
			kisszincsaladok[s] = kisszincsalad;
			add(kisszincsalad);
		}
		
		for (int s = 0; s < 4; s++)
		{
			Nagyszincsalad nagyszincsalad = new Nagyszincsalad(s);
			nagyszincsaladok[s] = nagyszincsalad;
			add(nagyszincsalad);
		}
		
		add(pagatfacan);
		add(sasfacan);
		add(xxiFogas);
		
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
					add(announcement);
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
				add(announcement);
			}
		}
		
		{
			Card card = new TarockCard(21);
			ultimok.put(card, new HashMap<Integer, Ultimo>());
			for (int roundIndex = 8; roundIndex >= 5; roundIndex--)
			{
				Ultimo announcement = new XXIUltimo(roundIndex);
				ultimok.get(card).put(roundIndex, announcement);
				add(announcement);
			}
		}
	}
}
