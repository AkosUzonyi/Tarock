package com.tisza.tarock.server;

import com.tisza.tarock.net.Connection;

public class Player
{
	private String name;
	private Connection connection;
	private int points;
	
	public Player(String name, Connection connection, int points)
	{
		this.name = name;
		this.connection = connection;
		this.points = points;
	}

	public String getName()
	{
		return name;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public int getPoints()
	{
		return points;
	}
	
	public void addPoints(int points)
	{
		this.points += points;
	}
}
