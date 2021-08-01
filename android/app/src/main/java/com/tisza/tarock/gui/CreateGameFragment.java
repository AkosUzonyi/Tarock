package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.model.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class CreateGameFragment extends MainActivityFragment
{
	public static final String TAG = "create_game";

	private static final String SHARED_PREF = "create_game_spinners";
	private static final String GAME_TYPE_KEY = "game_type";
	private static final String DOUBLE_ROUND_TYPE_KEY = "double_round_type";

	private Spinner gameTypeSpinner, doubleRoundTypeSpinner;
	private Button createButton;
	private ProgressDialog progressDialog;

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

		progressDialog = new ProgressDialog(getContext());
		progressDialog.setMessage(getString(R.string.lobby_create));
		progressDialog.setCancelable(false);

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		gameTypeSpinner.setSelection(sharedPreferences.getInt(GAME_TYPE_KEY, 0));
		doubleRoundTypeSpinner.setSelection(sharedPreferences.getInt(DOUBLE_ROUND_TYPE_KEY, 1));

		return view;
	}

	private void createButtonClicked()
	{
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		sharedPreferences.edit()
				.putInt(GAME_TYPE_KEY, gameTypeSpinner.getSelectedItemPosition())
				.putInt(DOUBLE_ROUND_TYPE_KEY, doubleRoundTypeSpinner.getSelectedItemPosition())
				.apply();

		String gameType = GameType.values()[gameTypeSpinner.getSelectedItemPosition()].getID();
		String doubleRoundType = DoubleRoundType.values()[doubleRoundTypeSpinner.getSelectedItemPosition()].getID();

		connectionViewModel.getApiInterface().createGameSession(new CreateGameSessionDTO(gameType, doubleRoundType)).subscribe(response ->
		{
			progressDialog.dismiss();
			getMainActivity().getSupportFragmentManager().popBackStack();

			//TODO: get location
			//String location = response.header("Location");
			//getMainActivity().joinGameSession(Integer.parseInt(location.substring(location.lastIndexOf('/') + 1)));
		});

		progressDialog.show();
	}
}
