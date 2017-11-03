package com.tisza.tarock.message;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

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
