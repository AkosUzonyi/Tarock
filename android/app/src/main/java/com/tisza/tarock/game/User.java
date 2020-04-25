package com.tisza.tarock.game;

import com.tisza.tarock.*;

public class User implements Comparable<User>
{
	private final int id;
	private final String name;
	private final String imgURL;
	private final boolean isFriend;
	private final boolean online;
	private final boolean bot;

	public User(int id, String name, String imgURL, boolean isFriend, boolean online, boolean bot)
	{
		if (name == null)
			throw new IllegalArgumentException("name == null");

		this.id = id;
		this.name = name;
		this.imgURL = imgURL;
		this.isFriend = isFriend;
		this.online = online;
		this.bot = bot;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getImageURL()
	{
		return imgURL;
	}

	public boolean isFriend()
	{
		return isFriend;
	}

	public boolean isOnline()
	{
		return online;
	}

	public boolean isBot()
	{
		return bot;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
			return true;

		if (!(other instanceof User))
			return false;

		User otherUser = (User)other;

		return id == otherUser.id;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	public boolean areContentsTheSame(User other)
	{
		return id == other.id && Utils.equals(name, other.name) && Utils.equals(imgURL, other.imgURL) && isFriend == other.isFriend && online == other.online && bot == other.bot;
	}

	@Override
	public int compareTo(User other)
	{
		if (isFriend != other.isFriend)
			return isFriend ? -1 : 1;

		if (online != other.online)
			return online ? -1 : 1;

		return id - other.id;
	}
}
