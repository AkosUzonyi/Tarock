package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

public interface ActionSender
{
	default void announce(Announcement announcementContra) {}
	default void announcePassz() {}
	default void bid(int bid) {}
	default void call(Card card) {}
	default void change(List<Card> cards) {}
	default void playCard(Card card) {}
	default void readyForNewGame() {}
	default void throwCards() {}
}
