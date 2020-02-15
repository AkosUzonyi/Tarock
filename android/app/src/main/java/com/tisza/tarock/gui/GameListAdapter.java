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
	private GameAdapterListener gameAdapterListener;
	private final LayoutInflater inflater;
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
			return oldItem.getId() == newItem.getId() && oldItem.getUsers().equals(newItem.getUsers()) && oldItem.getType().equals(newItem.getType());
		}
	};

	public GameListAdapter(Context context, GameAdapterListener gameAdapterListener)
	{
		super(gameInfoItemCallback);
		this.gameAdapterListener = gameAdapterListener;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<GameInfo> list, Integer userID)
	{
		this.userID = userID;
		list = new ArrayList<>(list);
		Collections.sort(list, this::compareGames);
		submitList(list);
	}

	private int compareGames(GameInfo g0, GameInfo g1)
	{
		if (userID != null && g0.containsUser(userID) != g1.containsUser(userID))
			return g0.containsUser(userID) ? -1 : 1;

		if (g0.getState() != g1.getState())
			return g0.getState().ordinal() - g1.getState().ordinal();

		return g0.getId() - g1.getId();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = inflater.inflate(R.layout.game_list_entry, parent, false);

		ViewHolder holder = new ViewHolder(view);
		holder.userNameViews[0] = view.findViewById(R.id.username0);
		holder.userNameViews[1] = view.findViewById(R.id.username1);
		holder.userNameViews[2] = view.findViewById(R.id.username2);
		holder.userNameViews[3] = view.findViewById(R.id.username3);
		holder.joinGameButton = view.findViewById(R.id.join_game_button);
		holder.deleteGameButton = view.findViewById(R.id.delete_game_button);

		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		GameInfo gameInfo = getItem(position);

		for (int i = 0; i < 4; i++)
		{
			holder.userNameViews[i].setText(i < gameInfo.getUsers().size() ? gameInfo.getUsers().get(i).getName() : "");
		}

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

		holder.joinGameButton.setText(joinButtonText);
		holder.joinGameButton.setTypeface(null, isInteresting ? Typeface.BOLD : Typeface.NORMAL);
		holder.joinGameButton.setOnClickListener(v -> gameAdapterListener.joinGame(gameInfo.getId()));
		holder.deleteGameButton.setVisibility(deleteButtonVisible ? View.VISIBLE : View.GONE);
		holder.deleteGameButton.setOnClickListener(v -> gameAdapterListener.deleteGame(gameInfo.getId()));
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public TextView[] userNameViews = new TextView[4];
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
