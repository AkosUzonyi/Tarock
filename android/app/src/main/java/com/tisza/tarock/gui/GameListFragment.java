package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.R;

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
		gameRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		gameRecyclerView.setAdapter(gameListAdapter);

		gameListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
		{
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount)
			{
				for (int i = positionStart; i < positionStart + itemCount; i++)
				{
					if (i >= gameListAdapter.getItemCount())
						break;

					if (gameListAdapter.getItem(i).containsUser(connectionViewModel.getUserID().getValue()))
					{
						gameRecyclerView.smoothScrollToPosition(i);
						break;
					}
				}
			}
		});

		connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
		connectionViewModel.getGames().observe(this, v -> updateList());
		connectionViewModel.getUserID().observe(this, v -> updateList());

		Button createNewGameButton = view.findViewById(R.id.new_game_button);
		createNewGameButton.setOnClickListener(v -> getMainActivity().createNewGame());

		return view;
	}

	private void updateList()
	{
		gameListAdapter.setData(connectionViewModel.getGames().getValue(), connectionViewModel.getUserID().getValue());
	}
}
