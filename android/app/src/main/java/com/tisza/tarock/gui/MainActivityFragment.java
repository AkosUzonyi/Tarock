package com.tisza.tarock.gui;

import android.app.*;

public class MainActivityFragment extends Fragment
{
	public MainActivity getMainActivity()
	{
		return ((MainActivity)getActivity());
	}
}
