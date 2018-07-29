package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

public interface ActionSender
{
	public void announce(Announcement announcementContra);
	public void announcePassz();
	public void bid(int bid);
	public void call(Card card);
	public void change(List<Card> cards);
	public void playCard(Card card);
	public void readyForNewGame();
	public void throwCards();
}
