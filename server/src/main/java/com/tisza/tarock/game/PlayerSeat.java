package com.tisza.tarock.game;

import java.util.*;
import java.util.stream.*;

public enum PlayerSeat
{
	SEAT0, SEAT1, SEAT2, SEAT3;

	public static PlayerSeat[] getAll()
	{
		return values();
	}

	public static PlayerSeat fromInt(int n)
	{
		return values()[n];
	}

	public int asInt()
	{
		return ordinal();
	}

	public PlayerSeat nextPlayer()
	{
		switch (this)
		{
			case SEAT0: return SEAT1;
			case SEAT1: return SEAT2;
			case SEAT2: return SEAT3;
			case SEAT3: return SEAT0;
		}
		throw new RuntimeException();
	}
}
