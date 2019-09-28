package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import com.tisza.tarock.*;
import com.tisza.tarock.R;

public class GameListFragment extends MainActivityFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.game_list, container, false);

		GameListAdapter gameListAdapter = new GameListAdapter(getActivity(), getMainActivity());
		ListView gameListView = view.findViewById(R.id.game_list);
		gameListView.setAdapter(gameListAdapter);

		ConnectionViewModel connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
		connectionViewModel.getGames().observe(this, gameListAdapter::setGames);

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
