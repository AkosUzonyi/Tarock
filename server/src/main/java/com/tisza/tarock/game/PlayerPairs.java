package com.tisza.tarock.game;

import java.util.*;

public class PlayerPairs
{
	private PlayerSeat caller, called;
	
	private List<PlayerSeat> callerTeam = new ArrayList<>();
	private List<PlayerSeat> opponentTeam = new ArrayList<>();

	public PlayerPairs(PlayerSeat caller, PlayerSeat called)
	{
		if (caller == null || called == null)
			throw new IllegalArgumentException();
		
		this.caller = caller;
		this.called = called;
		
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			(player == caller || player == called ? callerTeam : opponentTeam).add(player);
		}
	}
	
	public PlayerSeat getCaller()
	{
		return caller;
	}
	
	public PlayerSeat getCalled()
	{
		return called;
	}
	
	public Team getTeam(PlayerSeat player)
	{
		if (player == null)
			throw new IllegalArgumentException();
		
		return player == caller || player == called ? Team.CALLER : Team.OPPONENT;
	}
	
	public Collection<PlayerSeat> getPlayersInTeam(Team t)
	{
		return Collections.unmodifiableCollection(t == Team.CALLER ? callerTeam : opponentTeam);
	}
	
	public boolean isSolo()
	{
		return caller == called;
	}
}
