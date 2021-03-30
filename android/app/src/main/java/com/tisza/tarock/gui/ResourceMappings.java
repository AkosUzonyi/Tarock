package com.tisza.tarock.gui;

import android.annotation.*;
import android.content.*;
import android.content.res.*;
import android.util.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

@SuppressLint("UseSparseArrays")
public class ResourceMappings
{
	private static Context context;

	public static Map<Card, Integer> cardToImageResource = new HashMap<>();
	public static Map<Integer, String> bidToName = new HashMap<>();

	public static String[] trickNames;
	public static String[] contraNames;
	public static String[] tarockNames;
	public static String[] suitNames;
	public static String[] suitcardValueNames;
	public static Map<Card, String> cardToName = new HashMap<>();

	public static String silent;

	public static void init(Context c)
	{
		context = c;

		Resources resources = context.getResources();
		
		trickNames = resources.getStringArray(R.array.trick_array);
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
				Card card = Card.getSuitCard(s, v + 1);
				
				String imgName = suitNamesInFile[s] + valueNamesInFile[v];
				int id = resources.getIdentifier(imgName, "drawable", context.getPackageName());
				cardToImageResource.put(card, id);
				
				String name = suitNames[s] + " " + suitcardValueNames[v];
				cardToName.put(card, name);
			}
		}
		
		for (int i = 1; i <= 23; i++)
		{
			Card card = Card.getTarockCard(i);
			
			String imgName = "t" + i;
			int drawableID = resources.getIdentifier(imgName, "drawable", context.getPackageName());
			cardToImageResource.put(card, drawableID);
			
			cardToName.put(card, tarockNames[i - 1]);
		}

		bidToName.put(-2, resources.getString(R.string.bid_keep));
		bidToName.put(-1, resources.getString(R.string.bid_passz));
		for (int i = 0; i <= 3; i++)
		{
			int id = resources.getIdentifier("bid" + i, "string", context.getPackageName());
			bidToName.put(i, resources.getString(id));
		}
		
		silent = resources.getString(R.string.silent);
	}

	public static int getBitmapResForCard(Card card)
	{
		if (ResourceMappings.cardToImageResource.containsKey(card))
		{
			return cardToImageResource.get(card);
		}
		else
		{
			Log.e(GameFragment.LOG_TAG, card + " has no image");
			return R.drawable.card_back;
		}
	}

	public static String uppercaseCardName(Card card)
	{
		String name = cardToName.get(card);
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public static String getAnnouncementNameText(String name)
	{
		Resources resources = context.getResources();

		int announcementStringResID = resources.getIdentifier("announcement_" + name, "string", context.getPackageName());
		try
		{
			return resources.getString(announcementStringResID);
		}
		catch (Resources.NotFoundException e)
		{
			System.err.println("no string resource found for announcement: " + name);
			return null;
		}
	}
}
