package com.tisza.tarock.server.database;

import com.tisza.tarock.server.player.*;
import io.reactivex.*;

import java.util.*;

public class User
{
	private final int id;
	private final TarockDatabase database;

	private String name;

	public User(int id, TarockDatabase database, String name)
	{
		this.id = id;
		this.database = database;
		this.name = name;
	}

	public int getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Single<Optional<String>> getImageURL()
	{
		return database.getUserImgURL(id);
	}

	public void setImageURL(String imgURL)
	{
		database.setUserImgURL(id, imgURL);
	}

	public Single<Boolean> isFriendWith(User user)
	{
		return database.areUserFriends(id, user.id);
	}

	public Flowable<String> getFCMTokens()
	{
		return database.getFCMTokensForUser(id);
	}

	public boolean isBot()
	{
		return id < 0;
	}

	public Player createPlayer()
	{
		return isBot() ? new RandomPlayer(this, name, 500, 2000) : new ProtoPlayer(this, name);
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
