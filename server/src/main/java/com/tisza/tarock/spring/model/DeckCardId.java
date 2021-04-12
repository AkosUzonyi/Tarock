package com.tisza.tarock.spring.model;

import java.io.*;

public class DeckCardId implements Serializable
{
	public GameDB game;
	public int ordinal;

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DeckCardId that = (DeckCardId)o;

		if (game.id != that.game.id)
			return false;
		return ordinal == that.ordinal;
	}

	@Override
	public int hashCode()
	{
		int result = game.id;
		result = 31 * result + ordinal;
		return result;
	}
}
