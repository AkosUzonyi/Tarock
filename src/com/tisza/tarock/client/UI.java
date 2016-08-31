package com.tisza.tarock.client;

import java.util.*;

import com.tisza.tarock.card.*;

public interface UI
{
	public void startGame();
	public void setPlayerName(String name, Side s);
	public void setCards(PlayerCards pc);
	public void playerTurn(Side s);
	public void playerMessage(Side s);
	public void showAvailabeBids(List<Integer> bids);
	public void hideAvailabeBids();
	public void showAvailabeCalls(List<Integer> calls);
	public void hideAvailabeCalls();
	public void showCardsFromTalon(List<Card> cards);
	public void hideCardsFromTalon();
	public void setCardClickMode(CardClickMode mode);
	public void showAnnouncements();
	public void hideAnnouncements();
	public void setPlayedCard(Card c, Side s);
	public void animateCardsWon(Side s);
	public void hidePlayedCards();
	
	public static enum Side
	{
		BOTTOM, RIGHT, TOP, LEFT;
	}
	
	public static enum CardClickMode
	{
		CLICK, SELECT;
	}
}
