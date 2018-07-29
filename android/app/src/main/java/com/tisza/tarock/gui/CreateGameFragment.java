package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import java.util.*;

public class CreateGameFragment extends MainActivityFragment implements AvailableUsersAdapter.UsersSelectedListener
{
	private static final int SELECT_USER_COUNT = 3;

	private Spinner gameTypeSpinner, doubleRoundTypeSpinner;
	private AvailableUsersAdapter availableUsersAdapter;
	private Button createButton;
	private List<User> selectedUsers;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.create_new_game, container, false);

		gameTypeSpinner = view.findViewById(R.id.game_type_spinner);
		doubleRoundTypeSpinner = view.findViewById(R.id.double_round_type_spinner);

		createButton = view.findViewById(R.id.create_game_button);
		createButton.setOnClickListener(v -> createButtonClicked());

		usersSelected(Collections.EMPTY_LIST);

		availableUsersAdapter = getMainActivity().getAvailableUsersAdapter();
		availableUsersAdapter.setUsersSelectedListener(this);
		ListView availableUsersView = view.findViewById(R.id.available_users);
		availableUsersView.addHeaderView(new View(getActivity()));
		availableUsersView.setAdapter(availableUsersAdapter);

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

		MainProto.CreateGame.Builder builder = MainProto.CreateGame.newBuilder();

		builder.setType(Utils.gameTypeToProto(GameType.values()[gameTypeSpinner.getSelectedItemPosition()]));
		builder.setDoubleRoundType(Utils.doubleRoundTypeToProto(DoubleRoundType.values()[doubleRoundTypeSpinner.getSelectedItemPosition()]));

		for (User user : selectedUsers)
		{
			builder.addUserID(user.getId());
		}

		getMainActivity().getConnection().sendMessage(MainProto.Message.newBuilder().setCreateGame(builder).build());

		getMainActivity().getFragmentManager().popBackStack();
	}

	@Override
	public void usersSelected(List<User> users)
	{
		selectedUsers = users;

		int botCount = SELECT_USER_COUNT - selectedUsers.size();

		createButton.setEnabled(botCount >= 0);

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
