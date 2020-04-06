package com.tisza.tarock.gui;

import android.graphics.*;
import android.os.*;
import android.view.*;
import androidx.preference.*;
import com.tisza.tarock.R;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener
{
	public static final String TAG = "settings";

	private boolean restartActivity = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		view.setBackgroundColor(Color.WHITE);
		return view;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		setPreferencesFromResource(R.xml.settings, rootKey);
		findPreference("language").setOnPreferenceChangeListener(this);
		findPreference("notifications").setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		switch (preference.getKey())
		{
			case "language":
			case "notifications":
				restartActivity = true;
				break;
		}
		return true;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (restartActivity)
			getActivity().recreate();
	}
}
