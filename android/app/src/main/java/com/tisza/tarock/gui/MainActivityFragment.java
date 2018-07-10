package com.tisza.tarock.gui;

import android.app.*;
import android.os.*;

public class MainActivityFragment extends Fragment
{
	protected MainActivity mainActivity;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mainActivity = ((MainActivity)getActivity());
	}
}
