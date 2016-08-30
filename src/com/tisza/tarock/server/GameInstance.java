package com.tisza.tarock.server;

import com.tisza.tarock.game.*;

public class GameInstance
{
	public final int beginnerPlayer;
	public Dealing dealing = null;
	public Bidding bidding = null;
	public Changing changing = null;
	public Calling calling = null;
	public Announcing announcing = null;
	public Gameplay gameplay = null;
	
	public GameInstance(int beginnerPlayer)
	{
		this.beginnerPlayer = beginnerPlayer;
	}
}