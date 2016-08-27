package com.tisza.tarock.game;

public class GameHistory
{
	public final int beginnerPlayer;
	public Dealing dealing = null;
	public Bidding bidding = null;
	public Changeing changeing = null;
	public Calling calling = null;
	public Announcing announcing = null;
	public Gameplay gameplay = null;
	
	public GameHistory(int b)
	{
		beginnerPlayer = b;
	}
}
