package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

public interface Player
{
	public String getName();
	public EventHandler getEventHandler();
	public void setGame(Game game, PlayerSeat seat);
}
