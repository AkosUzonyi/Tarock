package com.tisza.tarock.gui;

import java.util.*;

import android.content.*;
import android.content.res.*;

import com.tisza.tarock.card.*;

public class ResourceMappings
{
	public static final Map<Card, Integer> cardToImageResource = new HashMap<Card, Integer>();
	
	public static void init(Context c)
	{
		String[] suitNames = new String[]{"h", "d", "s", "c"};
		String[] valueNames = new String[]{"10", "j", "c", "q", "k"};
		for (int s = 0; s < 4; s++)
		{
			for (int v = 0; v < 4; v++)
			{
				String imageName = suitNames[s] + valueNames[v];
				int id = c.getResources().getIdentifier(imageName, "drawable", c.getPackageName());
				cardToImageResource.put(new SuitCard(s, v + 1), id);
			}
		}
	}
}
