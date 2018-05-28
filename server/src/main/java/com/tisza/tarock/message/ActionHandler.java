package com.tisza.tarock.message;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public interface ActionHandler
{
	public void announce(PlayerSeat player, AnnouncementContra announcementContra);
	public void announcePassz(PlayerSeat player);
	public void bid(PlayerSeat player, int bid);
	public void call(PlayerSeat player, Card card);
	public void change(PlayerSeat player, List<Card> cards);
	public void playCard(PlayerSeat player, Card card);
	public void readyForNewGame(PlayerSeat player);
	public void throwCards(PlayerSeat player);
}
