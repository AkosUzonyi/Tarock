package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.concurrent.*;

public interface Player
{
	public String getName();
	public EventSender getEventSender();
	public void onAddedToGame(BlockingQueue<Action> actionQueue, PlayerSeat seat);
	public void onRemovedFromGame();
}
