package com.tisza.tarock.gui;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.R;
import com.tisza.tarock.game.*;
import com.tisza.tarock.proto.*;

import java.text.*;
import java.util.*;

public class CreateGameFragment extends MainActivityFragment
{
	public static final String TAG = "create_game";

	private static final String SHARED_PREF = "create_game_spinners";
	private static final String GAME_TYPE_KEY = "game_type";
	private static final String DOUBLE_ROUND_TYPE_KEY = "double_round_type";

	private Spinner gameTypeSpinner, doubleRoundTypeSpinner;
	private Button createButton;

	private ConnectionViewModel connectionViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.create_new_game, container, false);

		gameTypeSpinner = view.findViewById(R.id.game_type_spinner);
		doubleRoundTypeSpinner = view.findViewById(R.id.double_round_type_spinner);

		createButton = view.findViewById(R.id.create_game_button);
		createButton.setOnClickListener(v -> createButtonClicked());

		connectionViewModel.getGames().observe(this, this::onGameInfoUpdate);

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		gameTypeSpinner.setSelection(sharedPreferences.getInt(GAME_TYPE_KEY, 0));
		doubleRoundTypeSpinner.setSelection(sharedPreferences.getInt(DOUBLE_ROUND_TYPE_KEY, 1));

		return view;
	}

	private void onGameInfoUpdate(List<GameInfo> games)
	{
		Integer myUserID = connectionViewModel.getUserID().getValue();
		if (myUserID == null)
			return;

		for (GameInfo gameInfo : games)
		{
			if (gameInfo.getState() == GameSessionState.LOBBY && gameInfo.containsUser(myUserID))
			{
				getMainActivity().getSupportFragmentManager().popBackStack();
				getMainActivity().joinGame(gameInfo.getId());
				break;
			}
		}
	}

	private void createButtonClicked()
	{
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		sharedPreferences.edit()
				.putInt(GAME_TYPE_KEY, gameTypeSpinner.getSelectedItemPosition())
				.putInt(DOUBLE_ROUND_TYPE_KEY, doubleRoundTypeSpinner.getSelectedItemPosition())
				.apply();

		MainProto.CreateGameSession.Builder builder = MainProto.CreateGameSession.newBuilder();

		builder.setType(GameType.values()[gameTypeSpinner.getSelectedItemPosition()].getID());
		builder.setDoubleRoundType(DoubleRoundType.values()[doubleRoundTypeSpinner.getSelectedItemPosition()].getID());

		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setCreateGameSession(builder).build());
	}
}
