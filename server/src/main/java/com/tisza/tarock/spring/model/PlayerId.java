package com.tisza.tarock.spring.model;

import java.io.*;

public class PlayerId implements Serializable
{
	public int gameSessionId;
	public int ordinal;

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PlayerId that = (PlayerId)o;

		if (gameSessionId != that.gameSessionId)
			return false;
		return ordinal == that.ordinal;
	}

	@Override
	public int hashCode()
	{
		int result = gameSessionId;
		result = 31 * result + ordinal;
		return result;
	}
}
