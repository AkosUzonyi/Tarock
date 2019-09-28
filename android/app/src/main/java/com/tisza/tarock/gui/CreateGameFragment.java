package com.tisza.tarock.gui;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import com.tisza.tarock.R;
import com.tisza.tarock.game.*;
import com.tisza.tarock.proto.*;

import java.util.*;

public class CreateGameFragment extends MainActivityFragment implements AvailableUsersAdapter.UsersSelectedListener
{
	private static final String SHARED_PREF = "create_game_spinners";
	private static final String GAME_TYPE_KEY = "game_type";
	private static final String DOUBLE_ROUND_TYPE_KEY = "double_round_type";
	private static final String BOT_WARNING_IGNORED_KEY = "bot_warning_ignored";

	private static final int SELECT_USER_COUNT = 3;

	private Spinner gameTypeSpinner, doubleRoundTypeSpinner;
	private AvailableUsersAdapter availableUsersAdapter;
	private TextView botWarning;
	private boolean botWarningIgnored;
	private Button createButton;
	private Collection<User> selectedUsers;

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
		botWarning = view.findViewById(R.id.bot_warning);

		createButton = view.findViewById(R.id.create_game_button);
		createButton.setOnClickListener(v -> createButtonClicked());

		usersSelected(Collections.EMPTY_LIST);

		availableUsersAdapter = new AvailableUsersAdapter(getActivity());
		availableUsersAdapter.setUsersSelectedListener(this);
		connectionViewModel.getUsers().observe(this, availableUsersAdapter::setUsers);
		ListView availableUsersView = view.findViewById(R.id.available_users);
		availableUsersView.addHeaderView(inflater.inflate(R.layout.users_header, availableUsersView, false));
		availableUsersView.setAdapter(availableUsersAdapter);

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		gameTypeSpinner.setSelection(sharedPreferences.getInt(GAME_TYPE_KEY, 0));
		doubleRoundTypeSpinner.setSelection(sharedPreferences.getInt(DOUBLE_ROUND_TYPE_KEY, 0));
		botWarningIgnored = sharedPreferences.getBoolean(BOT_WARNING_IGNORED_KEY, false);
		botWarning.setVisibility(botWarningIgnored ? View.GONE : View.VISIBLE);

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		availableUsersAdapter.setUsersSelectedListener(null);
		availableUsersAdapter.clearSelectedUsers();
	}

	private void createButtonClicked()
	{
		if (selectedUsers.size() > SELECT_USER_COUNT)
			return;

		if (selectedUsers.size() == SELECT_USER_COUNT)
			botWarningIgnored = true;

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		sharedPreferences.edit()
				.putInt(GAME_TYPE_KEY, gameTypeSpinner.getSelectedItemPosition())
				.putInt(DOUBLE_ROUND_TYPE_KEY, doubleRoundTypeSpinner.getSelectedItemPosition())
				.putBoolean(BOT_WARNING_IGNORED_KEY, botWarningIgnored)
				.apply();

		MainProto.CreateGame.Builder builder = MainProto.CreateGame.newBuilder();

		builder.setType(GameType.values()[gameTypeSpinner.getSelectedItemPosition()].getID());
		builder.setDoubleRoundType(DoubleRoundType.values()[doubleRoundTypeSpinner.getSelectedItemPosition()].getID());

		for (User user : selectedUsers)
		{
			builder.addUserID(user.getId());
		}

		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setCreateGame(builder).build());

		getActivity().getSupportFragmentManager().popBackStack();
	}

	@Override
	public void usersSelected(Collection<User> users)
	{
		selectedUsers = users;

		int botCount = SELECT_USER_COUNT - selectedUsers.size();

		createButton.setEnabled(botCount >= 0);
		botWarning.setVisibility(botCount > 0 && !botWarningIgnored ? View.VISIBLE : View.GONE);

		if (botCount < 0)
		{
			createButton.setText(R.string.too_much_user_selected);
		}
		else if (botCount == 0)
		{
			createButton.setText(R.string.create_game);
		}
		else
		{
			createButton.setText(getResources().getQuantityString(R.plurals.create_game_with_bots, botCount, botCount));
		}
	}
}
