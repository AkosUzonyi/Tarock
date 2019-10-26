package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

import java.util.*;

public interface Game
{
	public void action(PlayerSeat seat, Action action);
	public void requestHistory(PlayerSeat seat, EventHandler eventHandler);
}
