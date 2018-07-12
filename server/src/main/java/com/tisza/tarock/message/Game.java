package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

import java.util.*;

public interface Game
{
	public void action(Action action);
	public void requestHistory(PlayerSeat seat, EventHandler eventHandler);
}
