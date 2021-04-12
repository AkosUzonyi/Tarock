package com.tisza.tarock.spring.model;

import java.io.*;

public class PlayerId implements Serializable
{
	public GameSessionDB gameSession;
	public int ordinal;

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PlayerId that = (PlayerId)o;

		if (gameSession.id != that.gameSession.id)
			return false;
		return ordinal == that.ordinal;
	}

	@Override
	public int hashCode()
	{
		int result = gameSession.id;
		result = 31 * result + ordinal;
		return result;
	}
}
