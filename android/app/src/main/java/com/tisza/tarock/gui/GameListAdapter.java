package com.tisza.tarock.gui;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class GameListAdapter extends ListAdapter<GameInfo, GameListAdapter.ViewHolder>
{
	private final Context context;
	private final LayoutInflater inflater;
	private GameAdapterListener gameAdapterListener;
	private Integer userID;

	private static final DiffUtil.ItemCallback<GameInfo> gameInfoItemCallback = new DiffUtil.ItemCallback<GameInfo>()
	{
		@Override
		public boolean areItemsTheSame(GameInfo oldItem, GameInfo newItem)
		{
			return oldItem.getId() == newItem.getId();
		}

		@Override
		public boolean areContentsTheSame(GameInfo oldItem, GameInfo newItem)
		{
			if (oldItem.getId() != newItem.getId() || oldItem.getType() != newItem.getType() || oldItem.getUsers().size() != newItem.getUsers().size())
				return false;

			for (int i = 0; i < oldItem.getUsers().size(); i++)
			{
				User oldUser = oldItem.getUsers().get(i);
				User newUser = newItem.getUsers().get(i);
				if (!oldUser.areContentsTheSame(newUser))
					return false;
			}

			return true;
		}
	};

	public GameListAdapter(Context context, GameAdapterListener gameAdapterListener)
	{
		super(gameInfoItemCallback);
		this.context = context;
		this.gameAdapterListener = gameAdapterListener;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<GameInfo> list, Integer userID)
	{
		this.userID = userID;
		List<GameInfo> filteredSortedList = new ArrayList<>();
		for (GameInfo gi : list)
			if (gi.getId() >= 0)
				filteredSortedList.add(gi);
		Collections.sort(filteredSortedList, this::compareGames);
		submitList(filteredSortedList);
	}

	@Override
	public GameInfo getItem(int position)
	{
		return super.getItem(position);
	}

	private int compareGames(GameInfo g0, GameInfo g1)
	{
		if (userID != null && g0.containsUser(userID) != g1.containsUser(userID))
			return g0.containsUser(userID) ? -1 : 1;

		if (g0.getState() != g1.getState())
			return g0.getState().ordinal() - g1.getState().ordinal();

		int onlineCount0 = 0;
		int onlineCount1 = 0;
		for (User u : g0.getUsers())
			if (u.isOnline() && !u.isBot())
				onlineCount0++;
		for (User u : g1.getUsers())
			if (u.isOnline() && !u.isBot())
				onlineCount1++;

		if (onlineCount0 != onlineCount1)
			return onlineCount1 - onlineCount0;

		return g0.getId() - g1.getId();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = inflater.inflate(R.layout.game_list_entry, parent, false);

		ViewHolder holder = new ViewHolder(view);
		holder.gameTypeTextView = view.findViewById(R.id.game_type);
		holder.usersRecyclerView = view.findViewById(R.id.game_users);
		holder.usersAdapter = new UsersAdapter(context, -1);
		holder.usersRecyclerView.setAdapter(holder.usersAdapter);
		holder.usersRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
		holder.joinGameButton = view.findViewById(R.id.join_game_button);
		holder.deleteGameButton = view.findViewById(R.id.delete_game_button);

		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		GameInfo gameInfo = getItem(position);

		holder.usersAdapter.setUsers(gameInfo.getUsers());

		int joinButtonText;
		if (gameInfo.getState() == GameSessionState.LOBBY)
			joinButtonText = R.string.lobby_enter;
		else if (gameInfo.containsUser(userID))
			joinButtonText = R.string.join_game;
		else
			joinButtonText = R.string.join_game_kibic;

		boolean deleteButtonVisible;
		if (gameInfo.getState() == GameSessionState.LOBBY)
			deleteButtonVisible = gameInfo.getUsers().size() > 0 && gameInfo.getUsers().get(0).getId() == userID;
		else
			deleteButtonVisible = gameInfo.containsUser(userID);

		boolean isInteresting = gameInfo.containsUser(userID) || gameInfo.getState() == GameSessionState.LOBBY;

		holder.gameTypeTextView.setText(context.getResources().getStringArray(R.array.game_type_array)[gameInfo.getType().ordinal()]);
		holder.joinGameButton.setText(joinButtonText);
		holder.joinGameButton.setTypeface(null, isInteresting ? Typeface.BOLD : Typeface.NORMAL);
		holder.joinGameButton.setOnClickListener(v -> gameAdapterListener.joinGame(gameInfo.getId()));
		holder.deleteGameButton.setVisibility(deleteButtonVisible ? View.VISIBLE : View.GONE);
		holder.deleteGameButton.setOnClickListener(v -> gameAdapterListener.deleteGame(gameInfo.getId()));
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public TextView gameTypeTextView;
		public RecyclerView usersRecyclerView;
		public UsersAdapter usersAdapter;
		public Button joinGameButton;
		public Button deleteGameButton;

		public ViewHolder(View itemView)
		{
			super(itemView);
		}
	}

	public interface GameAdapterListener
	{
		void joinGame(int gameID);
		void deleteGame(int gameID);
	}
}
