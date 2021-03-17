package com.tisza.tarock.spring.dto;

import com.tisza.tarock.spring.model.*;

import java.util.*;

public class GameDTO
{
	public int id;
	public String type;
	public int gameSessionId;
	public List<PlayerDB> players = new ArrayList<>();
	public long createTime;
}
