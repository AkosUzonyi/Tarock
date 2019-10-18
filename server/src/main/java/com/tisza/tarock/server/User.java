package com.tisza.tarock.server;

import java.util.*;

public class User
{
	private final int id;
	private final Database database;

	public User(int id, Database database)
	{
		this.id = id;
		this.database = database;
	}

	public int getID()
	{
		return id;
	}

	public String getName()
	{
		return database.getUserName(id);
	}

	public String getImageURL()
	{
		return database.getUserImgURL(id);
	}

	public boolean isFriendWith(User user)
	{
		return database.areUserFriends(id, user.id);
	}

	public Collection<String> getFCMTokens()
	{
		return database.getFCMTokensForUser(id);
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof User))
			return false;

		return id == ((User)obj).id;
	}
}
