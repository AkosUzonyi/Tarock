package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.model.*;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.*;
import io.reactivex.disposables.*;

import java.util.*;
import java.util.concurrent.*;

public class GameListFragment extends MainActivityFragment
{
	public static final String TAG = "game_list";

	private ConnectionViewModel connectionViewModel;
	private GameListAdapter gameListAdapter;
	private Disposable gameSessionListUpdateDisposable;

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

					if (gameListAdapter.getItem(i).containsUser(loggedInUser))
					{
						gameRecyclerView.smoothScrollToPosition(i);
						break;
					}
				}
			}
		});

		connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
		connectionViewModel.getLoggedInUser().observe(this, gameListAdapter::setUser);

		View downloadCsvButton = view.findViewById(R.id.download_csv_button);
		downloadCsvButton.setOnClickListener(v -> getMainActivity().downloadCsv());

		Button createNewGameButton = view.findViewById(R.id.new_game_button);
		createNewGameButton.setOnClickListener(v -> getMainActivity().createNewGame());

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		gameSessionListUpdateDisposable = Observable.interval(0, 2, TimeUnit.SECONDS)
				.flatMap(i -> connectionViewModel.getApiInterface().getGameSessions())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(gameListAdapter::setGameSessions);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (gameSessionListUpdateDisposable != null)
			gameSessionListUpdateDisposable.dispose();
	}
}
