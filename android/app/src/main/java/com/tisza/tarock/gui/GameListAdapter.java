package com.tisza.tarock.gui;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.api.model.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class GameListAdapter extends ListAdapter<GameSession, GameListAdapter.ViewHolder>
{
	private final Context context;
	private final LayoutInflater inflater;
	private GameAdapterListener gameAdapterListener;
	private Integer userID;

	private static final DiffUtil.ItemCallback<GameSession> gameSessionItemCallback = new DiffUtil.ItemCallback<GameSession>()
	{
		@Override
		public boolean areItemsTheSame(GameSession oldItem, GameSession newItem)
		{
			return oldItem.getId() == newItem.getId();
		}

		@Override
		public boolean areContentsTheSame(GameSession oldItem, GameSession newItem)
		{
			if (oldItem.getId() != newItem.getId() || oldItem.getType() != newItem.getType() || oldItem.getPlayers().size() != newItem.getPlayers().size())
				return false;

			for (int i = 0; i < oldItem.getPlayers().size(); i++)
			{
				User oldUser = oldItem.getPlayers().get(i).user;
				User newUser = newItem.getPlayers().get(i).user;
				if (!oldUser.areContentsTheSame(newUser))
					return false;
			}

			return true;
		}
	};

	public GameListAdapter(Context context, GameAdapterListener gameAdapterListener)
	{
		super(gameSessionItemCallback);
		this.context = context;
		this.gameAdapterListener = gameAdapterListener;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<GameSession> list, Integer userID)
	{
		this.userID = userID;
		List<GameSession> filteredSortedList = new ArrayList<>();
		for (GameSession gameSession : list)
			if (gameSession.getId() >= 0)
				filteredSortedList.add(gameSession);
		Collections.sort(filteredSortedList, this::compareGameSessions);
		submitList(filteredSortedList);
	}

	@Override
	public GameSession getItem(int position)
	{
		return super.getItem(position);
	}

	private int compareGameSessions(GameSession g0, GameSession g1)
	{
		if (userID != null && g0.containsUser(userID) != g1.containsUser(userID))
			return g0.containsUser(userID) ? -1 : 1;

		if (g0.getState() != g1.getState())
			return g0.getState().ordinal() - g1.getState().ordinal();

		int onlineCount0 = 0;
		int onlineCount1 = 0;
		int realPlayerCount0 = 0;
		int realPlayerCount1 = 0;
		for (Player p : g0.getPlayers())
		{
			if (p.user.isOnline() && !p.user.isBot())
				onlineCount0++;
			if (!p.user.isBot())
				realPlayerCount0++;
		}
		for (Player p : g1.getPlayers())
		{
			if (p.user.isOnline() && !p.user.isBot())
				onlineCount1++;
			if (!p.user.isBot())
				realPlayerCount1++;
		}

		if (onlineCount0 != onlineCount1)
			return onlineCount1 - onlineCount0;

		if (realPlayerCount0 != realPlayerCount1)
			return realPlayerCount1 - realPlayerCount0;

		return g1.getId() - g0.getId();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = inflater.inflate(R.layout.game_list_entry, parent, false);

		ViewHolder holder = new ViewHolder(view);
		holder.gameTypeTextView = view.findViewById(R.id.game_type);
		holder.usersRecyclerView = view.findViewById(R.id.game_users);
		holder.usersAdapter = new UsersAdapter(context);
		holder.usersAdapter.setImageVisible(false);
		holder.usersRecyclerView.setAdapter(holder.usersAdapter);
		holder.usersRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
		holder.joinGameButton = view.findViewById(R.id.join_game_button);
		holder.deleteGameButton = view.findViewById(R.id.delete_game_button);

		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		GameSession gameSession = getItem(position);

		List<User> users = new ArrayList<>();
		for (Player player : gameSession.getPlayers())
			users.add(player.user);
		holder.usersAdapter.setUsers(users);

		int joinButtonText;
		if (gameSession.getState() == GameSessionState.LOBBY)
			joinButtonText = R.string.lobby_enter;
		else if (gameSession.containsUser(userID))
			joinButtonText = R.string.join_game;
		else
			joinButtonText = R.string.join_game_kibic;

		boolean deleteButtonVisible;
		if (gameSession.getState() == GameSessionState.LOBBY)
			deleteButtonVisible = gameSession.getPlayers().size() > 0 && gameSession.getPlayers().get(0).user.id == userID;
		else
			deleteButtonVisible = gameSession.containsUser(userID);

		boolean isInteresting = gameSession.containsUser(userID) || gameSession.getState() == GameSessionState.LOBBY;

		holder.gameTypeTextView.setText(context.getResources().getStringArray(R.array.game_type_array)[gameSession.getType().ordinal()]);
		holder.joinGameButton.setText(joinButtonText);
		holder.joinGameButton.setTypeface(null, isInteresting ? Typeface.BOLD : Typeface.NORMAL);
		holder.joinGameButton.setOnClickListener(v -> gameAdapterListener.joinGameSession(gameSession.getId()));
		holder.deleteGameButton.setVisibility(deleteButtonVisible ? View.VISIBLE : View.GONE);
		holder.deleteGameButton.setOnClickListener(v -> gameAdapterListener.deleteGame(gameSession.getId()));
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
		void joinGameSession(int gameSessionID);
		void deleteGame(int gameSessionID);
	}
}
