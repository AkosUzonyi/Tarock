package com.tisza.tarock.game;

public class PlayerPairs
{
	private int caller, called;
	private boolean isSoloIntentional;

	public PlayerPairs(int caller, int called, boolean isSoloIntentional)
	{
		this.caller = caller;
		this.called = called;
		this.isSoloIntentional = isSoloIntentional;
	}
	
	public int getCaller()
	{
		return caller;
	}
	
	public boolean isCallerTeam(int p)
	{
		return p == caller || p == called;
	}
	
	public boolean isSolo()
	{
		return caller == called;
	}
	
	public boolean isSoloIntentional()
	{
		return isSoloIntentional;
	}
}
