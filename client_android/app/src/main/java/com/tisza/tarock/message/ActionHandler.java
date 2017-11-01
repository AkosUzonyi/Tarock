package com.tisza.tarock.message;

import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.game.AnnouncementContra;

public interface ActionHandler
{
	public void announce(int player, AnnouncementContra announcementContra);
	public void announcePassz(int player);
	public void bid(int player, int bid);
	public void call(int player, Card card);
	public void change(int player, List<Card> cards);
	public void playCard(int player, Card card);
	public void readyForNewGame(int player);
	public void throwCards(int player);
}
