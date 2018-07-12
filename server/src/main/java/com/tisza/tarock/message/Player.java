package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

public interface Player
{
	public String getName();
	public EventSender getEventSender();
	public void onAddedToGame(ActionHandler actionHandler, PlayerSeat seat);
	public void onRemovedFromGame();
}
