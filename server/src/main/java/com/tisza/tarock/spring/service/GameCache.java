package com.tisza.tarock.spring.service;

import com.tisza.tarock.game.phase.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class GameCache
{
	private Map<Integer, Game> games = new HashMap<>();

	private void loadGameFromDB(int gameID)
	{

	}
}
