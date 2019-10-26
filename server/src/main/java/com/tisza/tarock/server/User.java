package com.tisza.tarock.server;

import com.tisza.tarock.message.*;
import io.reactivex.*;

import java.util.*;
import java.util.concurrent.*;

public class User
{
	private final int id;
	private final TarockDatabase database;

	public User(int id, TarockDatabase database)
	{
		this.id = id;
		this.database = database;
	}

	public int getID()
	{
		return id;
	}

	public Single<String> getName()
	{
		return database.getUserName(id);
	}

	public Single<Optional<String>> getImageURL()
	{
		return database.getUserImgURL(id);
	}

	public Single<Boolean> isFriendWith(User user)
	{
		return database.areUserFriends(id, user.id);
	}

	public Flowable<String> getFCMTokens()
	{
		return database.getFCMTokensForUser(id);
	}

	public Single<Player> createPlayer(ScheduledExecutorService gameExecutorService)
	{
		return getName().map(name -> id < 0 ? new RandomPlayer(this, name, gameExecutorService, 500, 2000) : new ProtoPlayer(this, name));
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
