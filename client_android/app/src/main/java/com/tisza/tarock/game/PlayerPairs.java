package com.tisza.tarock.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayerPairs
{
	private int caller, called;
	
	private List<Integer> callerTeam = new ArrayList<Integer>();
	private List<Integer> opponentTeam = new ArrayList<Integer>();

	public PlayerPairs(int caller, int called)
	{
		if (!checkPlayerIndexValid(caller) || !checkPlayerIndexValid(called))
			throw new IllegalArgumentException();
		
		this.caller = caller;
		this.called = called;
		
		for (int i = 0; i < 4; i++)
		{
			(i == caller || i == called ? callerTeam : opponentTeam).add(i);
		}
	}
	
	public int getCaller()
	{
		return caller;
	}
	
	public int getCalled()
	{
		return called;
	}
	
	public Team getTeam(int player)
	{
		if (!checkPlayerIndexValid(player))
			throw new IllegalArgumentException();
		
		return player == caller || player == called ? Team.CALLER : Team.OPPONENT;
	}
	
	public Collection<Integer> getPlayersInTeam(Team t)
	{
		return Collections.unmodifiableCollection(t == Team.CALLER ? callerTeam : opponentTeam);
	}
	
	public boolean isSolo()
	{
		return caller == called;
	}
	
	private static boolean checkPlayerIndexValid(int p)
	{
		return p >= 0 && p < 4;
	}
}
