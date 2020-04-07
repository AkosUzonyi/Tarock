package com.tisza.tarock.gui;

import android.content.*;
import android.content.res.*;
import android.util.*;
import androidx.preference.*;

import java.util.*;

public class LocaleManager
{
	public static void updateLocale(Context context)
	{
		String localeCode = PreferenceManager.getDefaultSharedPreferences(context).getString("language", "hu");
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		Configuration config = resources.getConfiguration();
		config.setLocale(new Locale(localeCode.toLowerCase()));
		resources.updateConfiguration(config, dm);
	}
}
