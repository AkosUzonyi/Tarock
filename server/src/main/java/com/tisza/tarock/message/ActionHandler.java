package com.tisza.tarock.message;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.game.AnnouncementContra;

import java.util.List;

public interface ActionHandler
{
	public boolean announce(int player, AnnouncementContra announcementContra);
	public boolean announcePassz(int player);
	public boolean bid(int player, int bid);
	public boolean call(int player, Card card);
	public boolean change(int player, List<Card> cards);
	public boolean playCard(int player, Card card);
	public boolean readyForNewGame(int player);
	public boolean throwCards(int player);
}
