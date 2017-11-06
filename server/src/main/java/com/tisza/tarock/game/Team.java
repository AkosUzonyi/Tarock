package com.tisza.tarock.game;

public enum Team
{
	CALLER, OPPONENT;
	
	public Team getOther()
	{
		return this == CALLER ? OPPONENT : CALLER;
	}
}
