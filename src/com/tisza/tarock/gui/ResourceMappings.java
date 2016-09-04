package com.tisza.tarock.gui;

import java.util.*;

import android.content.*;

import com.tisza.tarock.*;
import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.net.packet.*;

public class ResourceMappings
{
	public static final Map<Card, Integer> cardToImageResource = new HashMap<Card, Integer>();
	public static final Map<Announcement, String> announcementToName = new HashMap<Announcement, String>();
	public static final Map<AnnouncementBase.Result, Integer> announcementResultToImage = new HashMap<AnnouncementBase.Result, Integer>();
	public static final Map<PacketLoginFailed.Reason, Integer> failureReasonToMessage = new HashMap<PacketLoginFailed.Reason, Integer>();
	
	public static void init(Context c)
	{
		String[] suitNames = new String[]{"a", "b", "c", "d"};
		String[] valueNames = new String[]{"1", "2", "3", "4", "5"};
		for (int s = 0; s < 4; s++)
		{
			for (int v = 0; v < 5; v++)
			{
				String imageName = suitNames[s] + valueNames[v];
				int id = c.getResources().getIdentifier(imageName, "drawable", c.getPackageName());
				cardToImageResource.put(new SuitCard(s, v + 1), id);
			}
		}
		
		for (int i = 1; i <= 22; i++)
		{
			int id = c.getResources().getIdentifier("t" + i, "drawable", c.getPackageName());
			cardToImageResource.put(new TarockCard(i), id);
		}
		
		for (Announcement a : Announcements.getAll())
		{
			announcementToName.put(a, a.getClass().getSimpleName());
		}
		
		announcementResultToImage.put(AnnouncementBase.Result.SUCCESSFUL, R.drawable.successful);
		announcementResultToImage.put(AnnouncementBase.Result.SUCCESSFUL_SILENT, R.drawable.successful);
		announcementResultToImage.put(AnnouncementBase.Result.FAILED, R.drawable.failed);
		announcementResultToImage.put(AnnouncementBase.Result.FAILED_SILENT, R.drawable.failed);
		
		
	}
}
