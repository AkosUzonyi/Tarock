package com.tisza.tarock.api.model;

import java.util.*;

public class GameDTO
{
	public int id;
	public String type;
	public int gameSessionId;
	public List<Player> players = new ArrayList<>();
	public long createTime;
}
