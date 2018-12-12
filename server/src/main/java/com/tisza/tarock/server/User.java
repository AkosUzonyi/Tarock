package com.tisza.tarock.server;

import com.tisza.tarock.message.*;

import java.util.*;

public class User
{
	private final String id;
	private String name;
	private String imgURL;
	private List<String> friendIDs = new ArrayList<>();

	private boolean loggedIn = false;
	private Map<Integer, ProtoPlayer> gameIDToPlayer = new HashMap<>();

	public User(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getImageURL()
	{
		return imgURL;
	}

	public void setImgURL(String imgURL)
	{
		this.imgURL = imgURL;
	}

	public boolean isFriendWith(User user)
	{
		return friendIDs.contains(user.getId());
	}

	public void addFriend(User user)
	{
		friendIDs.add(user.getId());
	}

	public void clearFriends()
	{
		friendIDs.clear();
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;
	}

	public ProtoPlayer createPlayerForGame(int gameID)
	{
		ProtoPlayer player = new ProtoPlayer(name);
		Player prev = gameIDToPlayer.put(gameID, player);
		if (prev != null)
			System.err.println("WARNING: player is overridden for a game");
		return player;
	}

	public ProtoPlayer getPlayerForGame(int gameID)
	{
		return gameIDToPlayer.get(gameID);
	}

	public void removePlayerForGame(int gameID)
	{
		gameIDToPlayer.remove(gameID);
	}
}
