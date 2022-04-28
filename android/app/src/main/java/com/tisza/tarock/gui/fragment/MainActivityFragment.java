package com.tisza.tarock.gui.fragment;

import androidx.fragment.app.*;
import com.tisza.tarock.gui.*;

public class MainActivityFragment extends Fragment
{
	public MainActivity getMainActivity()
	{
		return ((MainActivity)getActivity());
	}
}
