package com.tisza.tarock.gui;

import java.util.*;

import android.annotation.*;
import android.content.*;
import android.content.res.*;

import com.tisza.tarock.*;
import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;

@SuppressLint("UseSparseArrays")
public class ResourceMappings
{
	public static Map<Card, Integer> cardToImageResource = new HashMap<Card, Integer>();
	public static Map<PacketTurn.Type, String> turnAcitionToMessage = new HashMap<PacketTurn.Type, String>();
	public static Map<Integer, String> bidToName = new HashMap<Integer, String>();
	public static Map<Announcement, String> announcementToName = new HashMap<Announcement, String>();
	
	public static String[] roundNames;
	public static String[] contraNames;
	public static String[] tarockNames;
	public static String[] suitNames;
	public static String[] suitcardValueNames;
	public static Map<Card, String> cardToName = new HashMap<Card, String>();
	
	public static Map<AnnouncementBase.Result, Integer> announcementResultToImage = new HashMap<AnnouncementBase.Result, Integer>();
	public static Map<PacketLoginFailed.Reason, Integer> failureReasonToMessage = new HashMap<PacketLoginFailed.Reason, Integer>();
	
	private static String silent;
	private static String passz;
	
	public static String getAnnouncementContraName(AnnouncementContra ac)
	{
		if (ac == null)
			return passz;
		
		String result = "";
		
		if (!ac.isAnnounced())
		{
			result += silent;
		}
		else
		{
			result += ResourceMappings.contraNames[ac.getContraLevel()];
		}
		
		if (result.length() > 0)
			result += " ";
		
		result += ResourceMappings.announcementToName.get(ac.getAnnouncement());
		
		return result;
	}
	
	public static void init(Context context)
	{
		Resources resources = context.getResources();
		
		roundNames = resources.getStringArray(R.array.round_array);
		contraNames = resources.getStringArray(R.array.contra_array);
		tarockNames = resources.getStringArray(R.array.tarokk_array);
		suitNames = resources.getStringArray(R.array.suit_array);
		suitcardValueNames = resources.getStringArray(R.array.suitcard_value_array);
		
		String[] suitNamesInFile = new String[]{"a", "b", "c", "d"};
		String[] valueNamesInFile = new String[]{"1", "2", "3", "4", "5"};
		for (int s = 0; s < 4; s++)
		{
			for (int v = 0; v < 5; v++)
			{
				Card card = new SuitCard(s, v + 1);
				
				String imgName = suitNamesInFile[s] + valueNamesInFile[v];
				int id = resources.getIdentifier(imgName, "drawable", context.getPackageName());
				cardToImageResource.put(card, id);
				
				String name = suitNames[s] + " " + suitcardValueNames[v];
				cardToName.put(card, name);
			}
		}
		
		for (int i = 1; i <= 22; i++)
		{
			Card card = new TarockCard(i);
			
			String imgName = "t" + i;
			int drawableID = resources.getIdentifier(imgName, "drawable", context.getPackageName());
			cardToImageResource.put(card, drawableID);
			
			cardToName.put(card, tarockNames[i - 1]);
		}
		
		announcementResultToImage.put(AnnouncementBase.Result.SUCCESSFUL, R.drawable.successful);
		announcementResultToImage.put(AnnouncementBase.Result.SUCCESSFUL_SILENT, R.drawable.successful);
		announcementResultToImage.put(AnnouncementBase.Result.FAILED, R.drawable.failed);
		announcementResultToImage.put(AnnouncementBase.Result.FAILED_SILENT, R.drawable.failed);
		
		bidToName.put(-1, resources.getString(R.string.bid_passz));
		for (int i = 0; i <= 3; i++)
		{
			int id = resources.getIdentifier("bid" + i, "string", context.getPackageName());
			bidToName.put(i, resources.getString(id));
		}
		
		silent = resources.getString(R.string.silent);
		passz = resources.getString(R.string.passz);
		
		announcementToName.put(Announcements.game, resources.getString(R.string.jatek));
		announcementToName.put(Announcements.trull, resources.getString(R.string.trull));
		announcementToName.put(Announcements.xxiFogas, resources.getString(R.string.xxiFogas));
		for (int i = 0; i < 4; i++)
		{
			Banda banda = Announcements.bandak[i];
			announcementToName.put(banda, suitNames[i] + " " + resources.getString(R.string.banda));
		}
		announcementToName.put(Announcements.negykiraly, resources.getString(R.string.negykiraly));
		announcementToName.put(Announcements.dupla, resources.getString(R.string.dupla));
		announcementToName.put(Announcements.hosszuDupla, resources.getString(R.string.hosszuDupla));
		announcementToName.put(Announcements.nyolctarokk, resources.getString(R.string.nyolctarokk));
		announcementToName.put(Announcements.kilenctarokk, resources.getString(R.string.kilenctarokk));
		announcementToName.put(Announcements.szinesites, resources.getString(R.string.szinesites));
		announcementToName.put(Announcements.volat, resources.getString(R.string.volat));
		announcementToName.put(Announcements.centrum, roundNames[4]);
		announcementToName.put(Announcements.kismadar, roundNames[5]);
		announcementToName.put(Announcements.nagymadar, roundNames[6]);
		announcementToName.put(Announcements.kings[0], resources.getString(R.string.kiralyultimo));
		announcementToName.put(Announcements.kings[1], resources.getString(R.string.ketkiralyok));
		announcementToName.put(Announcements.kings[2], resources.getString(R.string.haromkiralyok));
		announcementToName.put(Announcements.zaroparos, resources.getString(R.string.zaroparos));
		announcementToName.put(Announcements.pagatfacan, resources.getString(R.string.pagatfacan));
		announcementToName.put(Announcements.sasfacan, resources.getString(R.string.sasfacan));
		for (int i = 0; i < 4; i++)
		{
			Szincsalad kisszincsalad = Announcements.kisszincsaladok[i];
			Szincsalad nagyszincsalad = Announcements.nagyszincsaladok[i];
			announcementToName.put(kisszincsalad, suitNames[i] + " " + resources.getString(R.string.kisszincsalad));
			announcementToName.put(nagyszincsalad, suitNames[i] + " " + resources.getString(R.string.nagyszincsalad));
		}
		
		for (Card card : Announcements.ultimok.keySet())
		{
			Map<Integer, Ultimo> map = Announcements.ultimok.get(card);
			for (int roundIndex : map.keySet())
			{
				String annName = cardToName.get(card) + " " + roundNames[roundIndex];
				announcementToName.put(map.get(roundIndex), annName);
			}
		}
		
		for (Announcement a : Announcements.getAll())
		{
			if (!announcementToName.containsKey(a)) announcementToName.put(a, a.getClass().getSimpleName());
		}
	}
}
