package com.tisza.tarock;

import android.support.annotation.*;

import java.util.*;

public class User implements Comparable<User>
{
	private final String id;
	private final String name;
	private final String imgURL;
	private final boolean isFriend;

	public User(String id, String name, String imgURL, boolean isFriend)
	{
		if (id == null || name == null)
			throw new IllegalArgumentException();

		this.id = id;
		this.name = name;
		this.imgURL = imgURL;
		this.isFriend = isFriend;
	}

	public String getId()
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

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
			return true;

		if (!(other instanceof User))
			return false;

		User otherUser = (User)other;

		return id.equals(otherUser.id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public int compareTo(User other)
	{
		if (isFriend != other.isFriend)
			return isFriend ? -1 : 1;

		return id.compareTo(other.id);
	}
}
