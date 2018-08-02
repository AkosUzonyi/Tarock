package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class TarokkFekszik extends ZebiSound
{
	public TarokkFekszik(Context context)
	{
		super(context, 1F, R.raw.tarokkfekszik);
	}

	@Override
	public void skartTarock(int[] counts)
	{
		for (int count : counts)
		{
			if (count > 0)
			{
				activate();
				break;
			}
		}
	}
}
