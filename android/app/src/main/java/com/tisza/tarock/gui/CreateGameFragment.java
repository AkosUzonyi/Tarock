package com.tisza.tarock.gui;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

public class CreateGameFragment extends MainActivityFragment
{
	private MainActivity mainActivity;
	private Spinner gameTypeSpinner;
	private AvailableUsersAdapter availableUsersAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.create_new_game, container, false);

		gameTypeSpinner = view.findViewById(R.id.game_type_spinner);

		availableUsersAdapter = mainActivity.getAvailableUsersAdapter();
		ListView availableUsersView = view.findViewById(R.id.available_users);
		availableUsersView.setAdapter(availableUsersAdapter);

		Button createButton = view.findViewById(R.id.create_game_button);
		createButton.setOnClickListener(v ->
		{
			if (availableUsersAdapter.getSelectedUsers().size() > 4)
			{
				Toast.makeText(mainActivity, R.string.too_much_user_selected, Toast.LENGTH_SHORT).show();
				return;
			}

			MainProto.CreateGame.Builder builder = MainProto.CreateGame.newBuilder();

			builder.setType(Utils.gameTypeToProto(GameType.values()[gameTypeSpinner.getSelectedItemPosition()]));

			for (User user : availableUsersAdapter.getSelectedUsers())
			{
				builder.addUserID(user.getId());
			}

			mainActivity.getConnection().sendMessage(MainProto.Message.newBuilder().setCreateGame(builder).build());

			mainActivity.getFragmentManager().popBackStack();
		});

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		availableUsersAdapter.clearSelectedUsers();
	}
}
