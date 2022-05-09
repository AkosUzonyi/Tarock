package com.tisza.tarock.gui.fragment;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.*;
import com.tisza.tarock.api.model.*;
import com.tisza.tarock.gui.adapter.*;
import com.tisza.tarock.gui.viewmodel.*;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.*;
import io.reactivex.disposables.*;

import java.util.concurrent.*;

public class GameListFragment extends MainActivityFragment implements GameListAdapter.GameAdapterListener
{
	public static final String TAG = "game_list";

	private final APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);;
	private AuthenticationViewModel authenticationViewModel;
	private GameListAdapter gameListAdapter;
	private Disposable gameSessionListUpdateDisposable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.game_list, container, false);

		gameListAdapter = new GameListAdapter(getActivity(), this);
		RecyclerView gameRecyclerView = view.findViewById(R.id.game_list);
		gameRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		gameRecyclerView.setItemAnimator(new DefaultItemAnimator());
		gameRecyclerView.setAdapter(gameListAdapter);

		gameListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
		{
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount)
			{
				User loggedInUser = authenticationViewModel.getLoggedInUser().getValue();
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

		authenticationViewModel = ViewModelProviders.of(getActivity()).get(AuthenticationViewModel.class);
		authenticationViewModel.getLoggedInUser().observe(this, gameListAdapter::setUser);

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
				.flatMap(i -> apiInterface.getGameSessions())
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

	@Override
	public void joinGameSession(int gameSessionID)
	{
		getMainActivity().joinGameSession(gameSessionID);
	}

	@Override
	public void deleteGame(int gameSessionID)
	{
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.delete_game_confirm)
				.setPositiveButton(R.string.delete_game, (dialog, which) -> apiInterface.deleteGameSession(gameSessionID).subscribe())
				.setNegativeButton(R.string.cancel, null)
				.show();
	}
}
