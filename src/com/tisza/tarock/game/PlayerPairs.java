package com.tisza.tarock.game;

import java.util.*;

public class PlayerPairs
{
	private int caller, called;
	private boolean isSoloIntentional;
	
	private List<Integer> callerTeam = new ArrayList<Integer>();
	private List<Integer> opponentTeam = new ArrayList<Integer>();

	public PlayerPairs(int caller, int called, boolean isSoloIntentional)
	{
		if (!checkPlayerIndexValid(caller) || !checkPlayerIndexValid(called))
			throw new IllegalArgumentException();
		
		this.caller = caller;
		this.called = called;
		this.isSoloIntentional = isSoloIntentional;
		
		for (int i = 0; i < 4; i++)
		{
			opponentTeam.add(i);
		}
		
		callerTeam.add((Integer)caller);
		opponentTeam.remove((Integer)caller);
		if (!isSolo())
		{
			callerTeam.add((Integer)called);
			opponentTeam.remove((Integer)called);
		}
	}
	
	public int getCaller()
	{
		return caller;
	}
	
	public Team getTeam(int p)
	{
		if (!checkPlayerIndexValid(caller) || !checkPlayerIndexValid(called))
			throw new IllegalArgumentException();
		
		return p == caller || p == called ? Team.CALLER : Team.OPPONENT;
	}
	
	public Collection<Integer> getPlayersInTeam(Team t)
	{
		return Collections.unmodifiableCollection(t == Team.CALLER ? callerTeam : opponentTeam);
	}
	
	public boolean isSolo()
	{
		return caller == called;
	}
	
	public boolean isSoloIntentional()
	{
		return isSoloIntentional;
	}
	
	private static boolean checkPlayerIndexValid(int p)
	{
		return p >= 0 && p < 4;
	}
}
