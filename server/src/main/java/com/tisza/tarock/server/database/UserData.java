package com.tisza.tarock.server.database;

public class UserData
{
	private int id;
	private String name, imgURL;

	public UserData(int id, String name, String imgURL)
	{
		this.id = id;
		this.name = name;
		this.imgURL = imgURL;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getImgURL()
	{
		return imgURL;
	}

	public boolean isBot()
	{
		return id < 4;
	}
}
