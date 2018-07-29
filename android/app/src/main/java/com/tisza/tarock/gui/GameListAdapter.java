package com.tisza.tarock.gui;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class GameListAdapter extends BaseAdapter
{
	private GameAdapterListener gameAdapterListener;
	private final LayoutInflater inflater;
	private List<GameInfo> games = new ArrayList<>();

	public GameListAdapter(Context context, GameAdapterListener gameAdapterListener)
	{
		this.gameAdapterListener = gameAdapterListener;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setGames(Collection<GameInfo> games)
	{
		this.games = new ArrayList<>(games);
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return games.size();
	}

	@Override
	public Object getItem(int position)
	{
		return games.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return games.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view;
		ViewHolder holder;

		if (convertView == null)
		{
			view = inflater.inflate(R.layout.game_list_entry, parent, false);

			holder = new ViewHolder();
			holder.userNameViews[0] = view.findViewById(R.id.username0);
			holder.userNameViews[1] = view.findViewById(R.id.username1);
			holder.userNameViews[2] = view.findViewById(R.id.username2);
			holder.userNameViews[3] = view.findViewById(R.id.username3);
			holder.joinGameButton = view.findViewById(R.id.join_game_button);
			holder.deleteGameButton = view.findViewById(R.id.delete_game_button);

			view.setTag(holder);
		}
		else
		{
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}

		GameInfo gameInfo = games.get(position);

		for (int i = 0; i < 4; i++)
		{
			holder.userNameViews[i].setText(gameInfo.getPlayerNames().get(i));
		}

		holder.joinGameButton.setOnClickListener(v -> gameAdapterListener.joinGame(gameInfo.getId()));
		holder.deleteGameButton.setOnClickListener(v -> gameAdapterListener.deleteGame(gameInfo.getId()));

		return view;
	}

	private static class ViewHolder
	{
		public TextView[] userNameViews = new TextView[4];
		public Button joinGameButton;
		public Button deleteGameButton;
	}

	public static interface GameAdapterListener
	{
		public void joinGame(int gameID);
		public void deleteGame(int gameID);
	}
}
