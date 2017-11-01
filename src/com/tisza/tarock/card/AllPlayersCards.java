package com.tisza.tarock.card;

import java.util.ArrayList;
import java.util.List;

public class AllPlayersCards
{
	private List<PlayerCards> list = new ArrayList<PlayerCards>();
	
	public AllPlayersCards()
	{
		for (int i = 0; i < 4; i++)
		{
			list.add(new PlayerCards());
		}
	}
	
	private AllPlayersCards(AllPlayersCards a)
	{
		for (PlayerCards pc : a.list)
		{
			list.add(pc.clone());
		}
	}
	
	public PlayerCards getPlayerCards(int player)
	{
		return list.get(player);
	}
	
	public AllPlayersCards clone()
	{
		return new AllPlayersCards(this);
	}
}
