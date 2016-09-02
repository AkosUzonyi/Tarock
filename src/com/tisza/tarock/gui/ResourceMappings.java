package com.tisza.tarock.gui;

import java.util.*;

import android.content.*;
import android.content.res.*;

import com.tisza.tarock.*;
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
			for (int v = 0; v < 5; v++)
			{
				String imageName = suitNames[s] + valueNames[v];
				int id = c.getResources().getIdentifier(imageName, "drawable", c.getPackageName());
				cardToImageResource.put(new SuitCard(s, v + 1), id);
			}
		}
		cardToImageResource.put(new TarockCard(1), R.drawable.i);
		cardToImageResource.put(new TarockCard(2), R.drawable.ii);
		cardToImageResource.put(new TarockCard(13), R.drawable.xiii);
		cardToImageResource.put(new TarockCard(19), R.drawable.xix);
		cardToImageResource.put(new TarockCard(20), R.drawable.xx);
		cardToImageResource.put(new TarockCard(21), R.drawable.xxi);
		cardToImageResource.put(new TarockCard(22), R.drawable.s);
	}
}
