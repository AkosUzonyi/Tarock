package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.facebook.*;
import com.facebook.login.widget.*;
import com.tisza.tarock.R;

public class GameListFragment extends MainActivityFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.game_list, container, false);

		ListView gameListView = view.findViewById(R.id.game_list);
		gameListView.setAdapter(getMainActivity().getGameListAdapter());

		Button createNewGameButton = view.findViewById(R.id.new_game_button);
		createNewGameButton.setOnClickListener(v -> getMainActivity().createNewGame());

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}
