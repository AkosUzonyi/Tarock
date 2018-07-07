package com.tisza.tarock;

import java.util.*;

public class User
{
	private final String id;
	private final String name;
	private final String imgURL;

	public User(String id, String name)
	{
		this(id, name, null);
	}

	public User(String id, String name, String imgURL)
	{
		if (id == null || name == null)
			throw new IllegalArgumentException();

		this.id = id;
		this.name = name;
		this.imgURL = imgURL;
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
}
