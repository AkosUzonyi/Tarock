package com.tisza.tarock.util.util;

import com.tisza.tarock.game.card.*;

import java.util.*;
import java.util.stream.*;

public class Util
{
	private Util()
	{

	}

	public static List<Card> createCardList(String... ids)
	{
		return Arrays.stream(ids).map(Card::fromId).collect(Collectors.toList());
	}
}
