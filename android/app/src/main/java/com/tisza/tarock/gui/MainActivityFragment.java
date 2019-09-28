package com.tisza.tarock.gui;

import androidx.fragment.app.*;

public class MainActivityFragment extends Fragment
{
	public MainActivity getMainActivity()
	{
		return ((MainActivity)getActivity());
	}
}
