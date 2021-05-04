package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;

import java.util.*;

public class Announcements
{
	private static final Map<String, Announcement> idmap = new HashMap<>();
	private static final List<Announcement> list = new ArrayList<>();

	private static final List<TrickAnnouncement> trickAnnouncements = new ArrayList<>();

	public static final Jatek jatek = new Jatek();
	public static final HivatalbolKontraParti hkp = new HivatalbolKontraParti();
	public static final Trull trull = new Trull();
	public static final Banda[] bandak = new Banda[4];
	public static final Negykiraly negykiraly = new Negykiraly();
	public static final TarockCount nyolctarokk = new TarockCount(8);
	public static final TarockCount kilenctarokk = new TarockCount(9);
	public static final Dupla dupla = new Dupla();
	public static final HosszuDupla hosszuDupla = new HosszuDupla();
	public static final KezbeVacak centrum = new KezbeVacak(4, Card.getTarockCard(20));
	public static final KezbeVacak kismadar = new KezbeVacak(5, Card.getTarockCard(21));
	public static final KezbeVacak nagymadar = new KezbeVacak(6, Card.getTarockCard(22));
	public static final Kings[] kings = new Kings[3];
	public static final Zaroparos zaroparos = new Zaroparos();
	public static final Szinesites szinesites = new Szinesites();
	public static final Volat volat = new Volat();
	public static final Szincsalad[] kisszincsaladok = new Szincsalad[4];
	public static final Szincsalad[] nagyszincsaladok = new Szincsalad[4];
	public static final ParosFacan parosfacan = new ParosFacan();
	public static final XXIFogas xxiFogas = new XXIFogas();
	public static final Map<Card, Map<Integer, Ultimo>> ultimok = new HashMap<>();

	public static Collection<Announcement> getAll()
	{
		return list;
	}

	public static Collection<TrickAnnouncement> getTrickAnnouncements()
	{
		return trickAnnouncements;
	}

	public static Announcement getByID(String id)
	{
		return idmap.get(id);
	}

	public static int getPosition(Announcement a)
	{
		return list.indexOf(a);
	}

	private static void add(Announcement announcement)
	{
		idmap.put(announcement.getID(), announcement);
		list.add(announcement);

		if (announcement instanceof TrickAnnouncement)
			trickAnnouncements.add((TrickAnnouncement)announcement);
	}

	static
	{
		add(jatek);
		add(hkp);
		add(nyolctarokk);
		add(kilenctarokk);
		add(trull);
		add(negykiraly);
		for (int s = 0; s < 4; s++)
		{
			bandak[s] = new Banda(s);
			add(bandak[s]);
		}
		add(dupla);
		add(hosszuDupla);
		add(centrum);
		add(kismadar);
		add(nagymadar);
		add(kings[0] = new Kings(1));
		add(zaroparos);
		add(szinesites);
		add(volat);
		add(kings[1] = new Kings(2));
		add(kings[2] = new Kings(3));
		add(parosfacan);
		add(xxiFogas);
		
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				SuitCard card = Card.getSuitCard(s, v);
				ultimok.put(card, new HashMap<>());
				for (int trickIndex = 8; trickIndex >= 5; trickIndex--)
				{
					Ultimo announcement = new Szinultimo(trickIndex, card);
					ultimok.get(card).put(trickIndex, announcement);
					add(announcement);
				}
			}
		}

		for (int t = 1; t <= 2; t++)
		{
			TarockCard card = Card.getTarockCard(t);
			ultimok.put(card, new HashMap<>());
			for (int trickIndex = 8; trickIndex >= 5; trickIndex--)
			{
				Ultimo announcement = new PagatSasUltimo(trickIndex, card);
				ultimok.get(card).put(trickIndex, announcement);
				add(announcement);
			}

			Ultimo announcement = new Facan(card);
			ultimok.get(card).put(0, announcement);
			add(announcement);
		}
		
		{
			Card card = Card.getTarockCard(21);
			ultimok.put(card, new HashMap<>());
			for (int trickIndex = 8; trickIndex >= 5; trickIndex--)
			{
				Ultimo announcement = new XXIUltimo(trickIndex);
				ultimok.get(card).put(trickIndex, announcement);
				add(announcement);
			}
		}

		for (int s = 0; s < 4; s++)
		{
			Szincsalad kisszincsalad = new Szincsalad(s, true);
			kisszincsaladok[s] = kisszincsalad;
			add(kisszincsalad);
		}

		for (int s = 0; s < 4; s++)
		{
			Szincsalad nagyszincsalad = new Szincsalad(s, false);
			nagyszincsaladok[s] = nagyszincsalad;
			add(nagyszincsalad);
		}
	}
}
