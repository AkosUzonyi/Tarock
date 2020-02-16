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
	private static final String BOT_WARNING_IGNORED_KEY = "bot_warning_ignored";

	private static final int SELECT_USER_COUNT = 3;

	private Spinner gameTypeSpinner, doubleRoundTypeSpinner;
	private UsersAdapter searchResultUsersAdapter;
	private UsersAdapter selectedUsersAdapter;
	private boolean botWarningIgnored;
	private Button createButton;
	private List<User> searchResultUsers = new ArrayList<>();
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

		userSearchView = view.findViewById(R.id.user_search);
		userSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String s)
			{
				updateSearchResultUsers();
				return true;
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

		searchResultUsersAdapter = new UsersAdapter(getContext(), R.drawable.ic_add_circle_black_40dp);
		searchResultUsersAdapter.setUsersSelectedListener(this::selectUser);
		searchResultUsersAdapter.setUsers(searchResultUsers);
		connectionViewModel.getUsers().observe(this, users -> updateSearchResultUsers());
		RecyclerView availableUsersView = view.findViewById(R.id.available_users);
		recyclerViewSetupCommon(availableUsersView);
		availableUsersView.setAdapter(searchResultUsersAdapter);

		selectedUsersAdapter = new UsersAdapter(getContext(), R.drawable.ic_remove_circle_black_40dp, SELECT_USER_COUNT);
		selectedUsersAdapter.setUsersSelectedListener(this::deselectUser);
		selectedUsersAdapter.setUsers(selectedUsers);
		RecyclerView selectedUsersView = view.findViewById(R.id.selected_users);
		recyclerViewSetupCommon(selectedUsersView);
		selectedUsersView.setAdapter(selectedUsersAdapter);

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		gameTypeSpinner.setSelection(sharedPreferences.getInt(GAME_TYPE_KEY, 0));
		doubleRoundTypeSpinner.setSelection(sharedPreferences.getInt(DOUBLE_ROUND_TYPE_KEY, 0));
		botWarningIgnored = sharedPreferences.getBoolean(BOT_WARNING_IGNORED_KEY, false);

		updateCreateButton();

		return view;
	}

	private void recyclerViewSetupCommon(RecyclerView recyclerView)
	{
		int duration = 50;
		DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
		itemAnimator.setAddDuration(duration);
		itemAnimator.setChangeDuration(duration);
		itemAnimator.setMoveDuration(duration);
		itemAnimator.setRemoveDuration(duration);

		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setItemAnimator(itemAnimator);
		recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
	}

	private void createButtonClicked()
	{
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
		sharedPreferences.edit()
				.putInt(GAME_TYPE_KEY, gameTypeSpinner.getSelectedItemPosition())
				.putInt(DOUBLE_ROUND_TYPE_KEY, doubleRoundTypeSpinner.getSelectedItemPosition())
				.putBoolean(BOT_WARNING_IGNORED_KEY, botWarningIgnored)
				.apply();

		MainProto.CreateGameSession.Builder builder = MainProto.CreateGameSession.newBuilder();

		builder.setType(GameType.values()[gameTypeSpinner.getSelectedItemPosition()].getID());
		builder.setDoubleRoundType(DoubleRoundType.values()[doubleRoundTypeSpinner.getSelectedItemPosition()].getID());

		for (User user : selectedUsers)
		{
			builder.addUserID(user.getId());
		}

		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setCreateGameSession(builder).build());

		getActivity().getSupportFragmentManager().popBackStack();
	}

	private static String normalizeString(CharSequence str)
	{
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
	}

	private void updateSearchResultUsers()
	{
		searchResultUsers.clear();

		List<User> availableUsers = connectionViewModel.getUsers().getValue();
		if (availableUsers == null)
			return;

		String queryNormalized = normalizeString(userSearchView.getQuery());
		for (User user : availableUsers)
		{
			boolean matchQuery = normalizeString(user.getName()).contains(queryNormalized);
			boolean alreadySelected = selectedUsers.contains(user);
			boolean isMe = connectionViewModel.getUserID().getValue() != null && user.getId() == connectionViewModel.getUserID().getValue();
			if (matchQuery && !alreadySelected && !isMe)
				searchResultUsers.add(user);
		}

		searchResultUsersAdapter.notifyDataSetChanged();
	}

	private void deselectUser(User user)
	{
		int position = selectedUsers.indexOf(user);
		if (position < 0)
			return;

		selectedUsers.remove(position);
		selectedUsersAdapter.notifyItemRemoved(position);
		searchResultUsersAdapter.notifyItemInserted(searchResultUsers.size());
		searchResultUsers.add(user);
		updateCreateButton();
	}

	private void selectUser(User user)
	{
		if (selectedUsers.size() >= SELECT_USER_COUNT)
			return;

		int searchResultPosition = searchResultUsers.indexOf(user);
		if (searchResultPosition < 0)
			return;

		selectedUsersAdapter.notifyItemChanged(selectedUsers.size());
		selectedUsers.add(user);
		searchResultUsers.remove(searchResultPosition);
		searchResultUsersAdapter.notifyItemRemoved(searchResultPosition);

		InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

		updateCreateButton();
	}

	private void updateCreateButton()
	{
		int freePlaceCount = SELECT_USER_COUNT - selectedUsers.size();
		createButton.setText(freePlaceCount == 0 ? getString(R.string.create_game) : getString(R.string.lobby_create, freePlaceCount));
	}
}
