package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

public interface Game
{
	public void action(Action action);
	public void requestHistory(PlayerSeat seat, EventSender eventSender);
}
