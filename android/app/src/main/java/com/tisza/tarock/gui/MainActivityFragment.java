package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.os.*;

public class MainActivityFragment extends Fragment
{
	public MainActivity getMainActivity()
	{
		return ((MainActivity)getActivity());
	}
}
