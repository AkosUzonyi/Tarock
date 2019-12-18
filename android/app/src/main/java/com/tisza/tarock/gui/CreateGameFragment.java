package com.tisza.tarock.gui;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import androidx.lifecycle.*;
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
	private static final String BOT_WARNING_IGNORED_KEY = "bot_warning_ignored";

	private static final int SELECT_USER_COUNT = 3;

	private Spinner gameTypeSpinner, doubleRoundTypeSpinner;
	private UsersAdapter searchResultUsersAdapter;
	private UsersAdapter selectedUsersAdapter;
	private TextView botWarning;
	private boolean botWarningIgnored;
	private Button createButton;
	private List<User> selectedUsers = new ArrayList<>();
	private SearchView userSearchView;

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

		userSearchView = view.findViewById(R.id.user_search);
		userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String s)
			{
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s)
			{
				updateSearchResultUsers();
				return true;
			}
		});

		createButton = view.findViewById(R.id.create_game_button);
		createButton.setOnClickListener(v -> createButtonClicked());

		searchResultUsersAdapter = new UsersAdapter(getActivity(), R.drawable.ic_add_circle_black_40dp);
		searchResultUsersAdapter.setUsersSelectedListener(this::selectUser);
		connectionViewModel.getUsers().observe(this, users -> updateSearchResultUsers());
		ListView availableUsersView = view.findViewById(R.id.available_users);
		availableUsersView.setAdapter(searchResultUsersAdapter);

		selectedUsersAdapter = new UsersAdapter(getActivity(), R.drawable.ic_remove_circle_black_40dp, SELECT_USER_COUNT);
		selectedUsersAdapter.setUsersSelectedListener(this::deselectUser);
		ListView selectedUsersView = view.findViewById(R.id.selected_users);
		selectedUsersView.addHeaderView(inflater.inflate(R.layout.users_header, selectedUsersView, false));
		selectedUsersView.setAdapter(selectedUsersAdapter);

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		gameTypeSpinner.setSelection(sharedPreferences.getInt(GAME_TYPE_KEY, 0));
		doubleRoundTypeSpinner.setSelection(sharedPreferences.getInt(DOUBLE_ROUND_TYPE_KEY, 0));
		botWarningIgnored = sharedPreferences.getBoolean(BOT_WARNING_IGNORED_KEY, false);
		botWarning.setVisibility(botWarningIgnored ? View.GONE : View.VISIBLE);

		updateSelectedUsers();

		return view;
	}

	private void createButtonClicked()
	{
		if (selectedUsers.size() != SELECT_USER_COUNT)
			return;

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

	private static String normalizeString(CharSequence str)
	{
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
	}

	private void updateSearchResultUsers()
	{
		List<User> searchResultUsers = new ArrayList<>();

		List<User> availableUsers = connectionViewModel.getUsers().getValue();
		if (availableUsers == null)
			return;

		String queryNormalized = normalizeString(userSearchView.getQuery());
		for (User user : availableUsers)
		{
			boolean matchQuery = normalizeString(user.getName()).contains(queryNormalized);
			boolean alreadySelected = selectedUsers.contains(user);
			if (matchQuery && !alreadySelected)
				searchResultUsers.add(user);
		}

		searchResultUsersAdapter.setUsers(searchResultUsers);
	}

	private void updateSelectedUsers()
	{
		selectedUsersAdapter.setUsers(selectedUsers);
		updateSearchResultUsers();

		if (selectedUsers.size() == SELECT_USER_COUNT)
		{
			createButton.setText(R.string.create_game);
			createButton.setEnabled(true);
		}
		else
		{
			createButton.setText(R.string.create_game_select_3);
			createButton.setEnabled(false);
		}
	}

	private void deselectUser(User user)
	{
		if (selectedUsers.remove(user))
			updateSelectedUsers();
	}

	private void selectUser(User user)
	{
		if (selectedUsers.size() >= SELECT_USER_COUNT)
			return;

		selectedUsers.add(user);
		updateSelectedUsers();
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}
}
