package com.tisza.tarock.server;

import com.tisza.tarock.message.*;

import java.util.*;

public class User
{
	private final String id;
	private String name;
	private String imgURL;
	private List<String> friendIDs = new ArrayList<>();
	private Set<String> fcmTokens = new HashSet<>();

	private boolean loggedIn = false;

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

	public void removeFriend(User user)
	{
		friendIDs.remove(user.getId());
	}

	public void clearFriends()
	{
		friendIDs.clear();
	}

	public void addFCMToken(String token)
	{
		fcmTokens.add(token);
	}

	public void removeFCMToken(String token)
	{
		fcmTokens.remove(token);
	}

	public Collection<String> getFCMTokens()
	{
		return fcmTokens;
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn)
	{
		this.loggedIn = loggedIn;

		System.out.println("user logged " + (loggedIn ? "in" : "out") + ": " + name + " (id: " + id + ")");
	}
}
