package com.tisza.tarock.message;

import com.tisza.tarock.game.GameState;
import com.tisza.tarock.proto.ActionProto.Action;

import java.util.stream.Collectors;

public class ActionDispatcher
{
	private final GameState gameState;

	public ActionDispatcher(GameState gameState)
	{
		this.gameState = gameState;
	}
}
