package com.tisza.tarock.api.model;

import com.tisza.tarock.*;

public class User implements Comparable<User>
{
	public int id;
	public String name;
	public String imgUrl;
	public boolean isBot;
	public boolean online = true;
	public long registrationTime;

	public User()
	{
	}

	public User(int id, String name, String imgUrl, boolean isBot, boolean online, long registrationTime)
	{
		this.id = id;
		this.name = name;
		this.imgUrl = imgUrl;
		this.isBot = isBot;
		this.online = online;
		this.registrationTime = registrationTime;
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
		return imgUrl;
	}

	public boolean isOnline()
	{
		return online;
	}

	public boolean isBot()
	{
		return isBot;
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
		return id == other.id && Utils.equals(name, other.name) && Utils.equals(imgUrl, other.imgUrl) && online == other.online && isBot == other.isBot;
	}

	@Override
	public int compareTo(User other)
	{
		if (online != other.online)
			return online ? -1 : 1;

		return id - other.id;
	}
}
