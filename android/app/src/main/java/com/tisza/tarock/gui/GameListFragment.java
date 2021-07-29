package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.model.*;

import java.util.*;

public class GameListFragment extends MainActivityFragment
{
	public static final String TAG = "game_list";

	private ConnectionViewModel connectionViewModel;
	private GameListAdapter gameListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.game_list, container, false);

		gameListAdapter = new GameListAdapter(getActivity(), getMainActivity());
		RecyclerView gameRecyclerView = view.findViewById(R.id.game_list);
		gameRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		gameRecyclerView.setItemAnimator(new DefaultItemAnimator());
		gameRecyclerView.setAdapter(gameListAdapter);

		gameListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
		{
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount)
			{
				User loggedInUser = connectionViewModel.getLoggedInUser().getValue();
				if (loggedInUser == null)
					return;

				for (int i = positionStart; i < positionStart + itemCount; i++)
				{
					if (i >= gameListAdapter.getItemCount())
						break;

					if (gameListAdapter.getItem(i).containsUser(loggedInUser.id))
					{
						gameRecyclerView.smoothScrollToPosition(i);
						break;
					}
				}
			}
		});

		connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
		connectionViewModel.getGameSessions().observe(this, v -> updateList());
		connectionViewModel.getLoggedInUser().observe(this, v -> updateList());

		View downloadCsvButton = view.findViewById(R.id.download_csv_button);
		downloadCsvButton.setOnClickListener(v -> getMainActivity().downloadCsv());

		Button createNewGameButton = view.findViewById(R.id.new_game_button);
		createNewGameButton.setOnClickListener(v -> getMainActivity().createNewGame());

		return view;
	}

	private void updateList()
	{
		List<GameSession> gameSessions = connectionViewModel.getGameSessions().getValue();
		User loggedInUser = connectionViewModel.getLoggedInUser().getValue();

		if (gameSessions == null)
			return;

		gameListAdapter.setData(gameSessions, loggedInUser == null ? 0 : loggedInUser.id);
	}
}
