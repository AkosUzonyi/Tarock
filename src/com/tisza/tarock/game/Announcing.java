package com.tisza.tarock.game;

import com.tisza.tarock.card.*;

public class Announcing
{
	private int currentPlayer;
	private int emptyAnnouncementsCount = 0;
	
	private AllPlayersCards playerHands;
	
	public Announcing(GameHistory gh)
	{
		currentPlayer = gh.bidding.getWinnerPlayer();
		
	}
}